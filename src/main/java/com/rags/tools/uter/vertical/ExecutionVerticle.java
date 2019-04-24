package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import com.rags.tools.uter.service.ExecutionService;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ragha on 26-07-2018.
 */

public class ExecutionVerticle extends AbstractRestServiceVerticle {

    public static Map<String, JsonObject> EXECUTIONS = new ConcurrentHashMap<>();

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();

        HttpClientOptions esClientOption = createClientOptions(false, config().getString("es.cluster.host"), config().getInteger("es.cluster.port"));

        eventBus.<JsonObject>consumer(RequestType.EXECUTE_UC.name(), message -> {
            JsonObject uc = message.body().getJsonObject("request");
            HttpClientOptions clientOption = createClientOptions(uc.getBoolean("ssl"), uc.getString("host"), uc.getInteger("port"));
            if (HttpMethod.POST.name().equals(uc.getString("type"))) {
                String payload = uc.getString("payload");
                executePostRequest(clientOption, message, uc.getString("path"), payload, ResponseType.valueOf(uc.getString("responseType")));
            } else if (HttpMethod.GET.name().equals(uc.getString("type"))) {
                executeGetRequest(clientOption, message, uc.getString("path"), ResponseType.valueOf(uc.getString("responseType")));
            } else {
                message.fail(9999, "Type " + uc.getString("type") + " not supported");
            }
        });

        eventBus.<JsonObject>consumer(RequestType.GET_ALL_EXECUTIONS.name(), message -> {
            JsonArray executions = new JsonArray();
            EXECUTIONS.forEach((key, value) -> executions.add(new JsonObject()
                    .put("ucId", value.getJsonObject("key").getString("ucId"))
                    .put("epoch", value.getJsonObject("key").getLong("epoch"))
                    .put("execution", value.getJsonObject("executions"))
                    .put("status", value.getString("status"))
                    .put("uc", value.getJsonObject("uc"))));
            message.reply(executions);
        });

        eventBus.<JsonObject>consumer(RequestType.START_EXECUTION.name(), message -> {
            JsonObject body = message.body();
            JsonObject data = new JsonObject().put("status", ExecutionStatus.RUNNING).put("uc", body.getJsonObject("uc")).put("key", body.getJsonObject("key"));
            EXECUTIONS.put(body.getString("id"), data);
            executePostRequest(esClientOption, message, "executions/" + body.getString("id"), data, ResponseType.OBJECT);
        });

        eventBus.<JsonObject>consumer(RequestType.FINISH_EXECUTION.name(), message -> {
            JsonObject body = message.body();
            JsonObject data = new JsonObject().put("status", ExecutionStatus.COMPLETED).put("uc", body.getJsonObject("uc")).put("key", body.getJsonObject("key")).put("executions", body.getJsonObject("executions"));
            EXECUTIONS.put(body.getString("id"), data);
            executePostRequest(esClientOption, message, "executions/" + body.getString("id"), data, ResponseType.OBJECT);
        });

        eventBus.<JsonObject>consumer(RequestType.FAIL_EXECUTION.name(), message -> {
            JsonObject body = message.body();
            JsonObject data = new JsonObject().put("status", ExecutionStatus.ABORT).put("uc", body.getJsonObject("uc")).put("key", body.getJsonObject("key")).put("executions", new JsonObject().put("status", "A"));
            EXECUTIONS.put(body.getString("id"), data);
            executePostRequest(esClientOption, message, "executions/" + body.getString("id"), data, ResponseType.OBJECT);
        });

    }

    public enum ExecutionStatus {RUNNING, COMPLETED, FAIL, ABORT}
}