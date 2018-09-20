package com.rags.tools.uter.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonBaseComparator {
    static Map<JsonKey, Map<Integer, List<JsonObject>>> compare(List<String> keys, JsonArray... arrs) {
        Map<JsonKey, Map<Integer, List<JsonObject>>> infoMap = new HashMap<>();
        int arrNo = 0;
        for(JsonArray jsArr : arrs) {
            jsArr.forEach(jsObj -> {
                JsonKey jsonKey = new JsonKey((JsonObject) jsObj, keys);
                infoMap.computeIfAbsent(jsonKey, v -> new HashMap<>());
                infoMap.get(jsonKey).computeIfAbsent(arrNo, v -> new LinkedList<>());
                infoMap.get(jsonKey).get(arrNo).add((JsonObject) jsObj);
            });
        }
        return infoMap;
    }
}
