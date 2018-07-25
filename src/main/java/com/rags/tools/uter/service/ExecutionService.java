package com.rags.tools.uter.service;

import com.rags.tools.uter.RequestType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by ragha on 26-07-2018.
 */
public class ExecutionService {

    public static Handler<RoutingContext> ucExecutionHandler() {
        return new ExecutionHandler();
    }

    static class ExecutionHandler extends AbstractRequestHandler<JsonObject, JsonObject> {

        public ExecutionHandler() {
            super("uc", RequestType.EXECUTE_UC);
        }

        @Override
        protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
            return body == null ? new JsonObject() : body.toJsonObject();
        }
    }

 /*   public static Handler<RoutingContext> ucsExecutionHandler() {
        return new UCSExecutionHandler();
    }

    static class UCSExecutionHandler extends AbstractRequestHandler<String, JsonObject> {

        public UCSExecutionHandler() {
            super("ucs", RequestType.GET_UCS);
        }

        @Override
        protected String getRequestData(HttpServerRequest request, Buffer body) {
            return request.getParam("id");
        }

        @Override
        protected void handleFuture(HttpServerRequest request, String requestData, Future<JsonObject> future, EventBus eventBus) {
            Future<Void> execFuture = Future.future();
            future.compose(executables->createExecutionsFuture(eventBus,executables,));
        }
    }*/


}
