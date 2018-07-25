package com.rags.tools.uter.service;

import io.vertx.core.json.JsonObject;

/**
 * Created by ragha on 22-04-2018.
 */
public class JsonUtil {
    public static JsonObject createFailedResponse(String message) {
        return createObject(null, false, false, message);
    }

    public static JsonObject createSuccessResponse(Object result) {
        return createObject(result, true, false, null);
    }

    private static JsonObject createObject(Object data, boolean success, boolean warning, String message) {
        return new JsonObject()
                .put("data", data)
                .put("success", success)
                .put("failed", !success)
                .put("warning", warning)
                .put("message", message);
    }
}
