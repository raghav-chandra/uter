package com.rags.tools.uter.vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class MatchingUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchingUtil.class);
    private static final int INFINITY = Integer.MAX_VALUE;
    
    private  static final String MATCH_PASS = "P";  
    private  static final String MATCH_FAIL = "F";  

    public JsonObject findBestMatchingAttrCount(JsonArray expected, JsonArray actual) {
        if (expected == null && actual == null) {
            return new JsonObject().put("status", MATCH_PASS);
        } else if (expected == null || actual == null) {
            return new JsonObject().put("status", MATCH_FAIL).put("exp", expected).put("act", actual).put("index", -1);
        }
        if (expected.isEmpty() && actual.isEmpty()) {
            return new JsonObject().put("status", MATCH_PASS);
        }

        /**
         * Handle Indexing of Array for Parallel Stream
         */
        AtomicInteger counter = new AtomicInteger(0);
        return expected.stream().map(exp -> {
            if (exp instanceof JsonObject) {
                JsonObject status = findBestMatchingAttrCount((JsonObject) exp, counter.get(), actual);
            }
            counter.set(counter.get() + 1);
            return new JsonObject();
        }).reduce(new JsonObject(), (accum, obj) -> {
            return accum;
        });
    }

    public JsonObject findBestMatchingAttrCount(JsonObject exp, int elemIndex, JsonArray array) {
        if (exp == null || array == null) {
            LOGGER.info("Either obj to match or array is null");
            return new JsonObject().put("status", MATCH_FAIL).put("exp", exp).put("count", -1).put("index", -1);
        }
        AtomicInteger bestMatch = new AtomicInteger(0);
        AtomicInteger bestMatchIndex = new AtomicInteger(-1);

        return array.stream().map(act -> {
            bestMatchIndex.set(bestMatchIndex.get() + 1);
            JsonObject status = new JsonObject().put("status", MATCH_FAIL).put("exp", exp).put("count", -1).put("index", -1);
            if (act instanceof JsonObject) {
                status = findBestMatchingAttrCount(exp, (JsonObject) act);
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
        }).reduce(new JsonObject().put("status", MATCH_FAIL).put("count", -1).put("elemIndex", elemIndex), (accum, obj) -> {
            /**
             * Return best matched data from all matching results.
             */
            if (obj.getString("status").equals(MATCH_PASS)) {
                accum.put("status", MATCH_PASS).put("index", obj.getInteger("index"));
            }
            if (accum.getString("status").equals(MATCH_FAIL) && accum.getInteger("count") <= obj.getInteger("count")) {
                obj.iterator().forEachRemaining(entry -> accum.put(entry.getKey(), entry.getValue()));
            }
            return accum;
        });
    }

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


    static boolean isPrimitive(Object o) {
        return o instanceof String || o instanceof Double || o instanceof Float || o instanceof Integer || o instanceof Boolean || o instanceof Long;
    }
}

