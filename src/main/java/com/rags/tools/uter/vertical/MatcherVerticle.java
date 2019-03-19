package com.rags.tools.uter.vertical;

import com.rags.tools.matcher.JsonMatcher;
import com.rags.tools.uter.RequestType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ragha on 20-07-2018.
 */
public class MatcherVerticle extends AbstractVerticle {

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        eventBus.<JsonObject>consumer(RequestType.MATCH_RESULTS.name(), message -> {
            Object actual = message.body().getValue("actual");
            Object expected = Json.decodeValue(message.body().getJsonObject("uc").getString("expected"), Object.class);
            if (expected instanceof Map) {
                message.reply(new JsonMatcher().compare(new JsonObject((Map) expected), (JsonObject) actual));
            } else if (expected instanceof List) {
                message.reply(new JsonMatcher().compare(new JsonArray((List) expected), (JsonArray) actual));
            }
        });
    }
}
