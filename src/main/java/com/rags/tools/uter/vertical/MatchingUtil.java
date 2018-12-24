package com.rags.tools.uter.vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MatchingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingUtil.class);
    private static final int INFINITY = Integer.MAX_VALUE;

    private static final String MATCH_PASS = "P";
    private static final String MATCH_FAIL = "F";

    public JsonObject findBestMatchingAttrCount(JsonArray expected, JsonArray actual) {
        JsonObject status = new JsonObject().put("status", MATCH_PASS);
        if (expected == null && actual == null) {
            return status;
        } else if (expected == null || actual == null) {
            return new JsonObject().put("status", MATCH_FAIL).put("exp", expected).put("act", actual).put("index", -1);
        } else if (expected.isEmpty() && actual.isEmpty()) {
            return status;
        }

        /**
         * Handle Indexing of Array for Parallel Stream
         * Depends on the content of Array, it'll be decided if we need to put any indexing at the object level or not for using parallel stream
         */
        AtomicInteger counter = new AtomicInteger(-1);
        JsonObject data = expected.stream().map(exp -> {
            counter.set(counter.get() + 1);
            return findBestMatchingAttrCount(exp, counter.get(), actual);
        }).collect(Collectors.toList()).parallelStream().reduce(new JsonObject().put("status", MATCH_FAIL).put("count", -1).put("diff", new JsonObject()), (accum, obj) -> {
            if (obj.getString("status").equals(MATCH_FAIL)) {
                accum.put("status", MATCH_FAIL);
            }
            return accum;
        });

        if (data.getString("status").equals(MATCH_PASS)) {

        }

        return data;
    }

    //We need to send all the data (1XN) so that the matching for the other elements can work properly.
    private JsonObject findBestMatchingAttrCount(Object exp, int elemIndex, JsonArray array) {
        if (exp == null || array == null) {
            LOGGER.info("Either obj to match or array is null");
            return new JsonObject().put("status", MATCH_FAIL).put("exp", exp).put("count", -1).put("index", -1);
        }
        AtomicInteger bestMatch = new AtomicInteger(0);
        AtomicInteger bestMatchIndex = new AtomicInteger(-1);

        return array.stream().map(act -> {
            bestMatchIndex.set(bestMatchIndex.get() + 1);
            JsonObject status = new JsonObject().put("status", MATCH_FAIL).put("exp", exp).put("count", -1).put("index", -1);
            if (isPrimitive(exp) && isPrimitive(act)) {
                if (exp.equals(act)) {
                    status.put("status", MATCH_PASS).put("count", 1);
                }
            } else if (exp instanceof JsonObject && act instanceof JsonObject) {
                status = findBestMatchingAttrCount((JsonObject) exp, (JsonObject) act);
            }

            if (status.getString("status").equals(MATCH_PASS)) {
                status.put("index", bestMatchIndex.get());
            }

            if (status.getString("status").equals(MATCH_FAIL) &&
                    status.getInteger("count") > 0 && bestMatch.get() <= status.getInteger("count")) {
                bestMatch.set(status.getInteger("count"));
                status.put("index", bestMatchIndex.get()).put("count", bestMatch.get()).put("diff", status.getJsonObject("diff"));
            }
            return status;
        }).collect(Collectors.toList()).parallelStream().reduce(new JsonObject().put("status", MATCH_FAIL).put("count", -1).put("elemIndex", elemIndex), (accum, obj) -> {

            // Return best matched data from all matching results.
            if (obj.getString("status").equals(MATCH_PASS)) {
                accum.put("status", MATCH_PASS).put("index", obj.getInteger("index"));
            }
            if (!accum.getString("status").equals(MATCH_PASS) && accum.getInteger("count") <= obj.getInteger("count")) {
                obj.iterator().forEachRemaining(entry -> accum.put(entry.getKey(), entry.getValue()));
            }
            return accum;
        });
    }

    //Check Status if its P its matching else check diff and see whats failing. Nested structure created
    public JsonObject findBestMatchingAttrCount(JsonObject exp, JsonObject act) {
        if (exp == null && act == null) {
            LOGGER.info("Either obj to match or actual is null");
            return new JsonObject().put("status", MATCH_PASS);
        } else if (exp == null || act == null) {
            return new JsonObject().put("status", MATCH_FAIL).put("act", act).put("exp", exp);
        }
        AtomicInteger matchingCount = new AtomicInteger(0);
        JsonObject finalStatusObj = new JsonObject();
        finalStatusObj.put("status", MATCH_PASS);
        JsonObject diff = new JsonObject();
        finalStatusObj.put("diff", diff);
        exp.iterator().forEachRemaining(item -> {
            String attr = item.getKey();
            Object expVal = item.getValue();
            Object actVal = act.getValue(attr);
            JsonObject internalDiff = new JsonObject().put("status", MATCH_PASS);
            diff.put(attr, internalDiff);
            if (expVal == null && actVal == null) {
                matchingCount.set(matchingCount.get() + 1);
            } else if (expVal == null || actVal == null) {
                internalDiff.put("status", MATCH_FAIL).put("exp", expVal).put("act", actVal);
                finalStatusObj.put("status", MATCH_FAIL);
            } else if (isPrimitive(expVal) && isPrimitive(actVal)) {
                boolean isMatching = expVal.equals(actVal);
                matchingCount.set(matchingCount.get() + (isMatching ? 1 : 0));
                if (!isMatching) {
                    internalDiff.put("status", MATCH_FAIL).put("exp", expVal).put("act", actVal);
                    finalStatusObj.put("status", MATCH_FAIL);
                }
            } else if (expVal instanceof JsonObject && actVal instanceof JsonObject) {
                JsonObject status = findBestMatchingAttrCount((JsonObject) expVal, (JsonObject) actVal);
                if (status.getString("status").equals(MATCH_PASS)) {
                    matchingCount.set(matchingCount.get() + 1);
                } else {
                    internalDiff.put("status", MATCH_FAIL).put("exp", expVal).put("act", actVal).put("diff", status.getJsonObject("diff"));
                    finalStatusObj.put("status", MATCH_FAIL);
                }
            } else if (expVal instanceof JsonArray && actVal instanceof JsonArray) {
                JsonArray expValArray = (JsonArray) expVal;
                JsonArray actValArray = (JsonArray) actVal;
                JsonObject status = findBestMatchingAttrCount(expValArray, actValArray);
                if (status.getString("status").equals(MATCH_PASS)) {
                    matchingCount.set(matchingCount.get() + 1);
                } else {
                    internalDiff.put("status", MATCH_FAIL).put("exp", expVal).put("act", actVal).put("diff", status.getJsonObject("diff"));
                    finalStatusObj.put("status", MATCH_FAIL);
                }
            }
        });
        if (finalStatusObj.getString("status").equals(MATCH_FAIL)) {
            finalStatusObj.put("status", MATCH_FAIL).put("act", act).put("exp", exp).put("count", matchingCount.get());
        }
        return finalStatusObj;
    }


    private boolean isPrimitive(Object o) {
        return o instanceof String || o instanceof Double || o instanceof Float || o instanceof Integer || o instanceof Boolean || o instanceof Long;
    }
}

