package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ragha on 26-07-2018.
 */
public class UseCaseVerticle extends AbstractDBVerticle {

    private static Map<String, JsonObject> UC = new ConcurrentHashMap<>();
    private static Map<String, List<String>> UCS = new ConcurrentHashMap<>();
    private static int ucCounter = 1;
    private static int ucsCounter = 1;

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();

        eventBus.<JsonObject>consumer(RequestType.CREATE_UC.name(), message -> {
            JsonObject uc = message.body().getJsonObject("uc");
            String id = "uc" + ucCounter++;
            uc.put("id", id);
            UC.put(id, uc);
            message.reply(uc);
        });

        eventBus.<JsonObject>consumer(RequestType.CREATE_UC.name(), message -> {
            JsonArray ucs = message.body().getJsonArray("ucs");
            String id = "ucs" + ucsCounter++;
            UCS.put(id, ucs.getList());
            message.reply(new JsonObject().put("id", id).put("ucs", ucs));
        });

        eventBus.<JsonObject>consumer(RequestType.GET_UCS.name(), message -> {
            String ucsId = message.body().getString("id");
            List<String> ucIds = UCS.get(ucsId);

            JsonArray uc = new JsonArray();
            ucIds.forEach(ucId -> uc.add(UC.get(ucId)));
            message.reply(new JsonObject().put("id", ucsId).put("uc", uc));
        });
    }
}
