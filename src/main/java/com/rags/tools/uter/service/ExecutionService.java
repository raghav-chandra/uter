package com.rags.tools.uter.service;

import com.rags.tools.uter.RequestType;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ragha on 26-07-2018.
 */
public class ExecutionService {

    public static Handler<RoutingContext> ucExecutionHandler() {
        return new ExecutionHandler();
    }

    static class ExecutionHandler extends AbstractRequestHandler<String, JsonObject> {
        ExecutionHandler() {
            super("uc", RequestType.FETCH_UC);
        }

        @Override
        protected String getRequestData(HttpServerRequest request, Buffer body) {
            return request.getParam("id");
        }

        @Override
        protected void handleFuture(HttpServerRequest request, String id, Future<JsonObject> retrieveFuture, EventBus eventBus, String cookie) {
            Future<Void> finalFuture = Future.future();
            final ExecutionKey key = new ExecutionKey(cookie, Instant.now().toEpochMilli(), id);
            final JsonObject executionData = new JsonObject().put("key", new JsonObject().put("ucId", id).put("epoch", key.getEpoch()));
            updateExecution(eventBus, key, RequestType.START_EXECUTION, executionData);
            retrieveFuture.compose(retrieveResult -> {
                List<Future> futures = new ArrayList<>(2);
                JsonObject uc = retrieveResult.getJsonObject("uc");
                updateExecution(eventBus, key, RequestType.START_EXECUTION, executionData.put("uc", uc));
                JsonObject reqObj = createApiRequest("bep", cookie, uc);
                futures.add(createFuture(eventBus, reqObj, RequestType.EXECUTE_UC, cookie, (requestObj, result) -> requestObj.put("result", result)));
                if ("BAT".equals(uc.getString("BAndTComp"))) {
                    JsonObject tepReqObj = createApiRequest("tep", cookie, uc);
                    futures.add(createFuture(eventBus, tepReqObj, RequestType.EXECUTE_UC, cookie, (requestObj, result) -> requestObj.put("result", result)));
                }
                return CompositeFuture.all(futures);
            }).compose(apiResults -> {
                JsonObject benchResult = apiResults.resultAt(0);
                JsonObject uc = benchResult.getJsonObject("uc");
                JsonObject matchingReq = new JsonObject()
                        .put("expected", Json.decodeValue(uc.getString("expected"), Object.class))
                        .put("actual", benchResult.getValue("result"));
                if ("BAT".equals(uc.getString("BAndTComp"))) {
                    JsonObject expectedResult = apiResults.resultAt(1);
                    matchingReq = new JsonObject()
                            .put("expected", benchResult.getValue("result"))
                            .put("actual", expectedResult.getValue("result"));
                }
                return createFuture(eventBus, matchingReq, RequestType.MATCH_RESULTS, cookie, (ResultHandler<JsonObject>) (reqObj, res) -> res.put("uc", uc).put("act", reqObj.getValue("actual")).put("exp", reqObj.getValue("expected")));
            }).compose(matcherResult -> {
                updateExecution(eventBus, key, RequestType.FINISH_EXECUTION, executionData.put("executions", matcherResult));
                onSuccess(request, matcherResult);
            }, finalFuture.setHandler(handler -> {
                        if (handler.failed()) {
                            handler.cause().printStackTrace();
                            updateExecution(eventBus, key, RequestType.FAIL_EXECUTION, executionData);
                            onFailure(request, handler.cause());
                        }
                    }
            ));
        }

        private JsonObject createApiRequest(String type, String cookie, JsonObject uc) {
            return new JsonObject()
                    .put("Cookie", cookie)
                    .put("request", new JsonObject()
                            .put("ssl", uc.getBoolean(type + "ssl"))
                            .put("type", uc.getString(type + "type"))
                            .put("host", uc.getString(type + "host"))
                            .put("port", uc.getInteger(type + "port"))
                            .put("path", uc.getString(type + "path"))
                            .put("responseType", uc.getString("responseType"))
                            .put("payload", uc.getString("payload")))
                    .put("uc", uc);
        }
    }

    static void updateExecution(EventBus eventBus, ExecutionKey key, RequestType status, JsonObject data) {
        String id = key.getEpoch() + key.getUcId();
        eventBus.<JsonObject>send(status.name(), data.put("id", id));
    }

    static class GetExecutionsHandler extends AbstractRequestHandler<JsonObject, JsonArray> {
        GetExecutionsHandler() {
            super("criteria", RequestType.GET_ALL_EXECUTIONS);
        }

        @Override
        protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
            return body == null || body.toString().isEmpty() ? new JsonObject() : body.toJsonObject();
        }
    }

    private static Future<JsonObject> createFuture(EventBus eventBus, JsonObject request, RequestType requestType,
                                                   String cookie, ResultHandler resultHandler) {
        Future<JsonObject> future = Future.future();
        request.put(AbstractRequestHandler.COOKIE_STRING, cookie);

        eventBus.<JsonObject>send(requestType.name(), request, reply -> {
            if (reply.succeeded()) {
                future.complete(resultHandler.handle(request, reply.result().body()));
            } else {
                future.fail(reply.cause());
            }
        });
        return future;
    }

    public static Handler<RoutingContext> ucsExecutionHandler() {
        return new UCSExecutionHandler();
    }

    static class UCSExecutionHandler extends AbstractRequestHandler<String, JsonObject> {
        UCSExecutionHandler() {
            super("ucs", RequestType.FETCH_UCS);
        }

        @Override
        protected String getRequestData(HttpServerRequest request, Buffer body) {
            return request.getParam("id");
        }
    }

    public static Handler<RoutingContext> getExecutionsHandler() {
        return new GetExecutionsHandler();
    }

    interface ResultHandler<T> {
        JsonObject handle(JsonObject requestObj, T result);
    }

    public static class ExecutionKey {
        private String userId;
        private long epoch;
        private String ucId;

        ExecutionKey(String userId, long epoch, String ucId) {
            this.userId = userId;
            this.epoch = epoch;
            this.ucId = ucId;
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(ucId).append(epoch).append(ucId).toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj instanceof ExecutionKey)) {
                return false;
            }
            ExecutionKey key = (ExecutionKey) obj;
            return new EqualsBuilder().append(userId, key.userId).append(epoch, key.epoch).append(ucId, key.ucId).isEquals();
        }

        public String getUserId() {
            return userId;
        }

        public long getEpoch() {
            return epoch;
        }

        public String getUcId() {
            return ucId;
        }
    }
}
