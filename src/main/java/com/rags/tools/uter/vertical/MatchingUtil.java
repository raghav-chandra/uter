package com.rags.tools.uter.vertical;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MatchingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingUtil.class);
    private static final int NEG_INFINITY = Integer.MIN_VALUE;
    private static final String MATCH_PASS = "P";
    private static final String MATCH_FAIL = "F";
    private static final String MATCH_NOT_EXISTS = "NE";

    public JsonObject findBestMatchingAttrCount(JsonArray expected, JsonArray actual) {
        JsonObject status = new JsonObject().put("status", MATCH_PASS);
        if (expected == null && actual == null) {
            return status;
        } else if (expected == null || actual == null) {
            return new JsonObject().put("status", MATCH_FAIL).put("exp", expected).put("act", actual).put("index", -1);
        } else if (expected.isEmpty() && actual.isEmpty()) {
            return status;
        }

        /*** * Handle Indexing of Array for Parallel Stream * Depends on the content of Array,
         *  it'll be decided if we need to put any indexing at the object level or not for using parallel stream */
        AtomicInteger counter = new AtomicInteger(-1);
        List<List<JsonObject>> crossResults = expected.stream().map(exp -> {
            counter.set(counter.get() + 1);
            return findBestMatchingAttrCount(exp, counter.get(), actual);
        }).collect(Collectors.toList());
        return calculateBestMatching(expected, actual, crossResults);
    }

    private JsonObject calculateBestMatching(JsonArray expected, JsonArray actual, List<List<JsonObject>> crossResults) {
        boolean[][] matrix = new boolean[expected.size()][actual.size()];
        JsonObject diff = new JsonObject();
        AtomicBoolean finalStatus = new AtomicBoolean(true);
        List<List<JsonObject>> nonMatching = new LinkedList<>(); /*//Matcher run for the full match*/
        crossResults.forEach(obj -> {
            List<JsonObject> matchingObjs = obj.stream().filter(data -> data.getString("status").equals(MATCH_PASS)).collect(Collectors.toList());
            boolean matching = !matchingObjs.isEmpty();
            finalStatus.set(finalStatus.get() && matching);
            if (matching) {
                blockActualColumn(matrix, matchingObjs.get(0));
                diff.put(String.valueOf(obj.iterator().next().getInteger("elemIndex")), matchingObjs.get(0));
            } else {
                nonMatching.add(obj);
            }
        }); /*//Matcher run for the missed match*/
        nonMatching.forEach(obj -> diff.put(String.valueOf(obj.iterator().next().getInteger("elemIndex")), findBestMatchedItemAndPopulateMatrix(obj, matrix)));
        JsonObject finalObj = new JsonObject().put("status", finalStatus.get() ? MATCH_PASS : MATCH_FAIL);
        if (!finalStatus.get()) {
            finalObj.put("act", actual).put("exp", expected).put("diff", diff);
        }
        return finalObj;
    }

    private void blockActualColumn(boolean[][] matrix, JsonObject matchingObj) {
        for (int i = 0; i < matrix.length; i++) {
            matrix[i][matchingObj.getInteger("index")] = true;
        }
    }

    private JsonObject findBestMatchedItemAndPopulateMatrix(List<JsonObject> allMatches, boolean[][] matrix) {
        List<JsonObject> sorted = allMatches.stream().sorted(Comparator.comparingInt(o -> o.getInteger("count"))).collect(Collectors.toList());
        JsonObject matchedObj = sorted.stream().filter(obj -> !matrix[obj.getInteger("elemIndex")][obj.getInteger("index")]).findFirst().orElse(null);
        if (matchedObj != null) {
            blockActualColumn(matrix, matchedObj);
        } else {
            matchedObj = new JsonObject().put("status", MATCH_NOT_EXISTS);
        }
        return matchedObj;
    }

     /*//We need to send all the data (1XN) so that the matching for the other elements can work properly.*/
    private List<JsonObject> findBestMatchingAttrCount(Object exp, int elemIndex, JsonArray array) {
        if (exp == null || array == null) {
            LOGGER.info("Either obj to match or array is null");
            return new ArrayList<>();
        }
        AtomicInteger bestMatchIndex = new AtomicInteger(-1);
        return array.stream().map(act -> {
            bestMatchIndex.set(bestMatchIndex.get() + 1);
            JsonObject status = new JsonObject().put("status", MATCH_FAIL).put("count", 0).put("index", bestMatchIndex.get()).put("elemIndex", elemIndex);
            if (isPrimitive(exp) && isPrimitive(act)) {
                if (exp.equals(act)) {
                    status.put("status", MATCH_PASS).put("count", NEG_INFINITY);
                }
            } else if (exp instanceof JsonObject && act instanceof JsonObject) {
                status = findBestMatchingAttrCount((JsonObject) exp, (JsonObject) act).put("index", bestMatchIndex.get()).put("elemIndex", elemIndex);
            }
            if (status.getString("status").equals(MATCH_FAIL)) {
                status.put("index", bestMatchIndex.get())
                /*.put("count", bestMatch.get())*/
                        .put("exp", exp).put("act", act).put("diff", status.getJsonObject("diff"));
            }
            return status;
        }).collect(Collectors.toList());
    }

    /*//Check Status if its P its matching else check diff and see whats failing. Nested structure created*/
    public JsonObject findBestMatchingAttrCount(JsonObject exp, JsonObject act) {
        if (exp == null && act == null) {
            LOGGER.info("Either obj to match or actual is null");
            return new JsonObject().put("status", MATCH_PASS).put("count", NEG_INFINITY);
        } else if (exp == null || act == null) {
            return new JsonObject().put("status", MATCH_FAIL).put("act", act).put("exp", exp);
        }
        AtomicInteger
                matchingCount = new AtomicInteger(0);
        JsonObject finalStatusObj = new JsonObject().put("status", MATCH_PASS).put("count", NEG_INFINITY);
        JsonObject diff = new
                JsonObject();
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

