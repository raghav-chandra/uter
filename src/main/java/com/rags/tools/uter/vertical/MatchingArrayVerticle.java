package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchingArrayVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingArrayVerticle.class);
    private static final int INFINITY = Integer.MAX_VALUE;

    @Override
    public void start() {
        EventBus eventBus = getVertx().eventBus();
        WorkerExecutor workerExecutor = getVertx().createSharedWorkerExecutor("ArrayElementMatcher", 100);
        eventBus.<JsonObject>consumer(RequestType.MATCH_ARRAY.name(), message -> {
                    JsonArray expected = message.body().getJsonArray("expected");
                    JsonArray actual = message.body().getJsonArray("actual");
                    AtomicInteger counter = new AtomicInteger(0);
                    List<Future> futures = new LinkedList<>();
                    expected.forEach(elem -> {
                        Future<JsonObject> future = Future.future();
                        JsonObject req = new JsonObject().put("elemIndex", counter.get()).put("array", actual).put("element", elem);
                        eventBus.<JsonObject>send(RequestType.MATCH_ARRAY_ELEMENT.name(), req, handler -> {
                            if (handler.succeeded()) {
                                future.complete(handler.result().body());
                            } else {
                                future.fail(handler.cause());
                            }
                        });
                        counter.set(counter.get() + 1);
                        futures.add(future);
                    });
                    final List<List<Integer>> matrix = new ArrayList<>(expected.size());
                    for (int i = 0; i < expected.size(); i++) {
                        matrix.add(i, new ArrayList<>(actual.size()));
                        for (int j = 0; j < actual.size(); i++) {
                            matrix.get(i).add(j, -1);
                        }
                    }
                    CompositeFuture.all(futures).setHandler(handler -> {
                                if (handler.succeeded()) {
                                    MatcherVerticle.FinalStatus status = MatcherVerticle.FinalStatus.PASS;
                                    for (int i = 0; i < expected.size(); i++) {
                                        JsonObject result = handler.result().resultAt(i);
                                        if (result.getInteger("count") != INFINITY) {
                                            status = MatcherVerticle.FinalStatus.FAIL;
                                        }
                                        if (result.getInteger("matchinglndex") >= 0) {
                                            matrix.get(result.getInteger("elemIndex")).add(result.getInteger("matchinglndex", result.getInteger("count")));
                                        }
                                    }
                                    message.reply(new JsonObject().put("finalStatus", status).put("matrix", matrix));
                                } else {
                                    message.fail(524, handler.cause().getMessage());
                                }
                            }

                    );
                }

        );
        eventBus.<JsonObject>consumer(RequestType.MATCH_ARRAY_ELEMENT.name(), message -> {
                    int elemIndex = message.body().getInteger("elemIndex");
                    JsonObject element = message.body().getJsonObject("element");
                    JsonArray array = message.body().getJsonArray("array");
                    workerExecutor.<JsonObject>executeBlocking(handler -> {
                                MatchingStatus status = findBestMatchingAttrCount(element, array, workerExecutor);
                                handler.complete(new JsonObject().put("elemIndex", elemIndex).put("finalStatus", status.getFinalStatus())
                                        .put("count", status.getMatchCount()).put("matchinglndex", status.getMatchingIndex()));
                            },
                            resHandler -> {
                                if (resHandler.succeeded()) {
                                    message.reply(resHandler.result());
                                } else {
                                    message.fail(7500, "Matching failed with reason : " + resHandler.cause().getMessage());
                                }
                            });
                }
        );
    }

    private MatchingStatus findBestMatchingAttrCount(JsonObject obj, JsonArray array, WorkerExecutor workerExecutor) {
        if (obj == null || array == null) {
            LOGGER.info("Either obj to match or array is null");
            return new MatchingStatus(MatcherVerticle.FinalStatus.FAIL, 0, -1);
        }
        AtomicInteger bestMatch = new AtomicInteger(0);
        AtomicInteger bestMatchIndex = new AtomicInteger(-1);
        for (int i = 0; i < array.size(); i++) {
            Object elem = array.getValue(i);
            if (elem != null && elem instanceof JsonObject) {
                JsonObject arrElem = (JsonObject) elem;
                MatchingStatus matchingStatus = findBestMatchingAttrCount(obj, arrElem, workerExecutor);
                if (matchingStatus.getFinalStatus() == MatcherVerticle.FinalStatus.PASS) {
                    bestMatch.set(INFINITY);
                    bestMatchIndex.set(i);
                } else {
                    int matCount = matchingStatus.getMatchCount();
                    if (matCount > bestMatch.get()) {
                        bestMatch.set(matCount);
                        bestMatchIndex.set(i);
                    }
                }
            }
        }
        return new MatchingStatus(bestMatch.get() == INFINITY ? MatcherVerticle.FinalStatus.PASS : MatcherVerticle.FinalStatus.FAIL, bestMatch.get(), bestMatchIndex.get());
    }

    private MatchingStatus findBestMatchingAttrCount(JsonObject obj, JsonObject act, WorkerExecutor workerExecutor) {
        if (obj == null || act == null) {
            LOGGER.info("Either obj to match or actual is null");
            return new MatchingStatus(MatcherVerticle.FinalStatus.FAIL, 0, -1);
        }
        AtomicInteger bestMatch = new AtomicInteger(0);
        int count = obj.size();
        AtomicInteger matchingCount = new AtomicInteger(0);
        obj.iterator().forEachRemaining(item -> {
                    String attr = item.getKey();
                    Object expVal = item.getValue();
                    Object actVal = act.getValue(attr);
                    if (expVal == null && actVal == null) {
                        matchingCount.set(matchingCount.get() + 1);
                    } else if (isPrimitive(expVal) && isPrimitive(actVal)) {
                        matchingCount.set(matchingCount.get() + (expVal.equals(actVal) ? 1 : 0));
                    } else if (expVal instanceof JsonObject && actVal instanceof JsonObject) {
                        MatchingStatus status = findBestMatchingAttrCount((JsonObject) expVal, (JsonObject) actVal, workerExecutor);
                        if (status.getFinalStatus() == MatcherVerticle.FinalStatus.PASS) {
                            matchingCount.set(matchingCount.get() + (expVal.equals(actVal) ? 1 : 0));
                        }
                    } else if (expVal instanceof JsonArray && actVal instanceof JsonArray) {
                        JsonArray expValArray = (JsonArray) expVal;
                        JsonArray actValArray = (JsonArray) actVal;
                        if (expValArray.size() == actValArray.size()) {
                            expValArray.stream().parallel().forEach(expected -> {
                            });
                        }
                    }
                }

        );
        if (matchingCount.get() > bestMatch.get()) {
            bestMatch.set(matchingCount.get());
        }

        return new MatchingStatus(count != bestMatch.get() ? MatcherVerticle.FinalStatus.FAIL : MatcherVerticle.FinalStatus.PASS, bestMatch.get(), -1);
    }

    static boolean isPrimitive(Object o) {
        return o instanceof String || o instanceof Double || o instanceof Float || o instanceof Integer || o instanceof Boolean;
    }
}

class MatchingStatus {
    private final MatcherVerticle.FinalStatus finalStatus;
    private final int matchCount;
    private final int matchingIndex;

    MatchingStatus(MatcherVerticle.FinalStatus finalStatus, int matchCount, int matchingIndex) {
        this.finalStatus = finalStatus;
        this.matchCount = matchCount;
        this.matchingIndex = matchingIndex;
    }

    public MatcherVerticle.FinalStatus getFinalStatus() {
        return finalStatus;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public int getMatchingIndex() {
        return matchingIndex;
    }
}

