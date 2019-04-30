package com.rags.tools.uter.verticle;

import com.rags.tools.matcher.JsonMatcher;
import com.rags.tools.uter.RequestType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ragha on 20-07-2018.
 */
public class MatcherVerticle extends AbstractVerticle {

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        eventBus.<JsonObject>consumer(RequestType.MATCH_RESULTS.name(), message -> {
            Object actual = message.body().getValue("actual");
            Object expected = message.body().getValue("expected");
            if (expected instanceof JsonObject) {
                message.reply(new JsonMatcher().compare((JsonObject) expected, (JsonObject) actual));
            } else if (expected instanceof JsonArray) {
                message.reply(new JsonMatcher().compare((JsonArray) expected, (JsonArray) actual));
            }
        });
    }
}
