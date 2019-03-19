package com.rags.tools.uter.vertical;

import com.rags.tools.uter.RequestType;
import com.rags.tools.uter.service.ExecutionService;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ragha on 26-07-2018.
 */

public class ExecutionVerticle extends AbstractRestServiceVerticle {
    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();

        eventBus.<JsonObject>consumer(RequestType.EXECUTE_UC.name(), message -> {
            JsonObject uc = message.body().getJsonObject("uc");
            HttpClientOptions clientOption = createClientOptions(uc.getBoolean("bepssl"), uc.getString("bephost"), uc.getInteger("bepport"));
            if (HttpMethod.POST.name().equals(uc.getString("beptype"))) {
                executePostRequest(clientOption, message, uc.getString("beppath"), uc.getValue("payload"), ResponseType.valueOf(uc.getString("responseType")));
            } else if (HttpMethod.GET.name().equals(uc.getString("beptype"))) {
                executeGetRequest(clientOption, message, uc.getString("beppath"), ResponseType.valueOf(uc.getString("responseType")));
            } else {
                message.fail(9999, "Type " + uc.getString("beptype") + " not supported");
            }
        });

        eventBus.<JsonObject>consumer(RequestType.GET_ALL_EXECUTIONS.name(), message -> {
            JsonArray executions = new JsonArray();
            ExecutionService.EXECUTIONS.forEach((key, value) -> executions.add(new JsonObject()
                    .put("ucId", key.getUcId())
                    .put("epoch", key.getEpoch())
                    .put("execution", value.getJsonObject("executions"))
                    .put("status", value.getString("status"))));
            message.reply(executions);
        });
    }

}
