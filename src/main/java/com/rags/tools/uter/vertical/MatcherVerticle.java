package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ragha on 20-07-2018.
 */
public class MatcherVerticle extends AbstractVerticle {
    private static final String JUNK_KEY = "charagis having junk key";

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        eventBus.<JsonObject>consumer(RequestType.MATCH_RESULTS.name(), message -> {
            JsonObject actual = message.body().getJsonObject("actual");
            JsonObject expected = new JsonObject(message.body().getJsonObject("uc").getString("expected"));
            JsonObject matching = new JsonObject();
            FinalStatus finalStatus = match(expected, actual, matching, JUNK_KEY);
            message.reply(new JsonObject().put("matching", matching).put("finalStatus", finalStatus));
        });
    }


    protected FinalStatus match(JsonObject expected, JsonObject actual, JsonObject attributesToMark, String key) {
        AtomicReference<FinalStatus> status = new AtomicReference<>(FinalStatus.PASS);
        if (expected == null && actual == null && key == null) {
            return FinalStatus.FAIL;
        } else if (expected == null && actual == null && !JUNK_KEY.equals(key)) {
            attributesToMark.put(key, MatchStatus.MATCHING);
        }
        JsonObject markerAttribute = attributesToMark;
        if (!JUNK_KEY.equals(key)) {
            markerAttribute = new JsonObject();
            attributesToMark.put(key, markerAttribute);
        }
        if (expected != null && actual != null) {
            JsonObject finalMarkerAttribute = markerAttribute;
            expected.iterator().forEachRemaining(item -> {
                String attr = item.getKey();
                Object actVal = actual.getValue(attr);
                Object expVal = item.getValue();
                if (expVal == null && actVal == null) {
                    finalMarkerAttribute.put(attr, MatchStatus.MATCHING);
                } else if (expVal instanceof JsonObject && actVal instanceof JsonObject) {
                    FinalStatus stat = match(expected.getJsonObject(attr), actual.getJsonObject(attr), finalMarkerAttribute, attr);
                    if (FinalStatus.FAIL.equals(stat)) {
                        status.set(FinalStatus.FAIL);
                    }
                } else if (expVal instanceof JsonArray && actVal instanceof JsonArray) {
                    JsonObject bestMatch = new JsonObject();
                    FinalStatus stat = match(expected.getJsonArray(attr), actual.getJsonArray(attr), bestMatch);
                    if (FinalStatus.FAIL.equals(stat)) {
                        status.set(FinalStatus.FAIL);
                    } else {
                        finalMarkerAttribute.put(attr, bestMatch);
                    }
                } else if (expVal instanceof String && actVal instanceof String || expVal instanceof
                        Double && actVal instanceof Double || expVal instanceof Float && actVal instanceof Float || expVal instanceof Integer && actVal instanceof Integer ||
                        expVal instanceof Boolean && actVal instanceof Boolean) {
                    if (expVal.equals(actVal)) {
                        finalMarkerAttribute.put(attr, MatchStatus.MATCHING);
                    } else {
                        finalMarkerAttribute.put(attr, MatchStatus.NOT_MATCHING);
                        status.set(FinalStatus.FAIL);
                    }
                } else {
                    finalMarkerAttribute.put(attr, MatchStatus.CAN_NOT_COMPARE);
                    status.set(FinalStatus.FAIL);
                }
            });
        }

        return status.get();
    }

    // todo: Change the algorithm
    // Checking each iterators best match and removing would not work.
    // Check the scenario in MatcherVerticleTest.testArrayNotEqual1
    protected FinalStatus match(JsonArray expected, JsonArray actual, JsonObject bestMatch) {
        Map<Integer, List<Integer>> bestMatches = new HashMap<>();
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        expected.iterator().forEachRemaining(item -> {
                    bestMatches.put(counter.get(), new LinkedList<>());
                    FinalStatus status = match(item, actual, bestMatches, counter.get());
                    if (status == FinalStatus.PASS) {
                    } else {
                    }
                    counter.set(counter.get() + 1);
                }

        );
        return FinalStatus.PASS;
    }

    private MatchStatus validateFields(JsonArray expected, JsonArray actual) {
        if (expected == null && actual == null) {
            return MatchStatus.MATCHING;
        } else if (expected == null || actual == null) {
            return MatchStatus.NOT_MATCHING;
        } else if (expected.size() == 0 && actual.size() == 0) {
            return MatchStatus.MATCHING;
        } else if (expected.size() == 0 || actual.size() == 0) {
            return MatchStatus.NOT_MATCHING;
        }

        Object expectedObject = expected.getValue(0);
        Object actualObject = actual.getValue(0);

        if (!expectedObject.getClass().getName().equals(actualObject.getClass().getName())) {
            return MatchStatus.NOT_MATCHING;
        }


        if (expectedObject instanceof JsonObject) {
            Set<String> expkeys = ((JsonObject) expectedObject).getMap().keySet();
            Set<String> actualKeys = ((JsonObject) actualObject).getMap().keySet();
            if (expkeys.size() != actualKeys.size()) {
                return MatchStatus.NOT_MATCHING;
            }
            return expkeys.stream().filter(actualKeys::contains).count() == expkeys.size() ? MatchStatus.MATCHING : MatchStatus.NOT_MATCHING;
        }


        return MatchStatus.MATCHING;

    }

    protected FinalStatus match2(JsonArray expected, JsonArray actual, JsonObject bestMatch) {

        MatchStatus preCompareStatus = validateFields(expected, actual);

        if (preCompareStatus == MatchStatus.NOT_MATCHING) {
            return FinalStatus.FAIL;
        }



        return FinalStatus.PASS;
    }

    protected FinalStatus match(Object expVal, JsonArray actual, Map<Integer, List<Integer>> bestMatches, int index) {
        AtomicInteger counter = new AtomicInteger(0);
        AtomicInteger matchingMaxCount = new AtomicInteger(0);
        final AtomicReference<FinalStatus> finalStatus = new AtomicReference<>(FinalStatus.FAIL);
        actual.forEach((Object actVal) -> {
                    if (finalStatus.get() == FinalStatus.PASS) {
                        return;
                    }
                    if (expVal == null && actVal == null) {
                        bestMatches.get(index).add(counter.get());
                    } else if (expVal instanceof JsonObject && actVal instanceof JsonObject) {
                        JsonObject attributes = new JsonObject();
                        FinalStatus stat = match((JsonObject) expVal, (JsonObject) actVal, attributes, JUNK_KEY);
                        if (FinalStatus.PASS.equals(stat)) {
                            bestMatches.get(index).add(counter.get());
                            finalStatus.set(FinalStatus.PASS);
                        } else {
                            int matchingCounts = getMatchingCounts(attributes);
                            if (matchingCounts > matchingMaxCount.get()) {
                                bestMatches.get(index).add(counter.get());
                                matchingMaxCount.set(matchingCounts);
                            }
                        }
                    } else if (expVal instanceof String && actVal instanceof String
                            || expVal instanceof Double && actVal instanceof Double
                            || expVal instanceof Float && actVal instanceof Float
                            || expVal instanceof Integer && actVal instanceof Integer
                            || expVal instanceof Boolean && actVal instanceof Boolean) {
                        if (expVal.equals(actVal)) {
                            bestMatches.get(index).add(counter.get());
                            finalStatus.set(FinalStatus.PASS);
                        }
                    }

                    counter.set(counter.get() + 1);
                }

        );
        return finalStatus.get();
    }

    private int getMatchingCounts(JsonObject attributes) {
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        attributes.forEach(item -> {
            if (MatchStatus.MATCHING.name().equals(item.getValue())) {
                counter.set(counter.get() + 1);
            }
        });
        return counter.get();
    }

    protected enum FinalStatus {PASS, FAIL}

    protected enum MatchStatus {
        MATCHING, NOT_MATCHING, CAN_NOT_COMPARE
    }
}
