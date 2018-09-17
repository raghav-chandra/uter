package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 * Created by ragha on 26-07-2018.
 */
public class ExecutionVerticle extends AbstractDBVerticle {

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();

        eventBus.<JsonObject>consumer(RequestType.EXECUTE_UC.name(), message -> {
            //Execute UC and return result
            JsonObject uc = message.body().getJsonObject("uc");
            if ("POST".equals(uc.getString("type"))) {
                JsonObject payload = uc.getJsonObject("payload");
                //Execute Post request
            } else if ("GET".equals(uc.getString("type"))) {
                //Execute GET request
            } else {
                message.fail(9999, "Type " + uc.getString("type") + " not supported");
            }
        });

    }
}
