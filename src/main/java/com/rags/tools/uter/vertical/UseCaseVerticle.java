package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by ragha on 26-07-2018.
 */
public class UseCaseVerticle extends AbstractDBVerticle {
    private static Map<String, JsonObject> UC = new ConcurrentHashMap<>();
    private static Map<String, List<String>> UCS = new ConcurrentHashMap<>();
    private static int ucCounter = 1;
    private static int ucsCounter = 1;

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();

        eventBus.<JsonObject>consumer(RequestType.CREATE_UC.name(), message -> {
            JsonObject uc = message.body().getJsonObject("uc");
            String id = uc.getString("id");
            if (id == null) {
                id = "uc" + ucCounter++;
            }
            uc.put("id", id).put("path", "/ " + uc.getString("path"));
            UC.put(id, uc);
            message.reply(uc);
        });

        eventBus.<JsonObject>consumer(RequestType.CREATE_UCS.name(), message -> {
            JsonArray ucIds = message.body().getJsonArray("ucIds");
            String id = "ucs" + ucsCounter++;
            UCS.put(id, ucIds.getList());
            message.reply(new JsonObject().put("id", id).put("ucIds", ucIds));
        });

        eventBus.<JsonObject>consumer(RequestType.FETCH_UC.name(), message -> {
            String ucId = message.body().getString("uc");
            message.reply(new JsonObject().put("id", ucId).put("uc", UC.get(ucId)));
        });

        eventBus.<JsonObject>consumer(RequestType.FETCH_UCS.name(), message -> {
            String ucsId = message.body().getString("id");
            List<String> ucIds = UCS.get(ucsId);
            JsonArray uc = new JsonArray();
            ucIds.forEach(ucId -> uc.add(UC.get(ucId)));
            message.reply(new JsonObject().put("id", ucsId).put("uc", uc));
        });

        eventBus.<JsonObject>consumer(RequestType.GET_ALL_UC.name(), message -> {
            message.reply(new JsonArray(new ArrayList(UC.values())));
        });
        eventBus.<JsonObject>consumer(RequestType.LOOKUP_UC.name(), message -> {
            JsonObject criteria = message.body().getJsonObject("criteria");
            String searchString = criteria.getString("searchString");
            message.reply(new JsonArray(UC.values().stream().filter(val -> Json.encode(val).contains(searchString)).collect(Collectors.toList())));
        });
    }
}