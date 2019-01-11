package com.rags.tools.uter.service;

import com.rags.tools.uter.RequestType;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by ragha on 26-07-2018.
 */
public class ExecutionService {
    private static final String COOKIE_STRING = "Cookie";

    public static Map<ExecutionKey, JsonObject> EXECUTIONS = new ConcurrentHashMap<>();

    public static Handler<RoutingContext> ucExecutionHandler() {
        return new ExecutionHandler();
    }

    public static Handler<RoutingContext> ucsExecutionHandler() {
        return new UCSExecutionHandler();
    }

    public static Handler<RoutingContext> getExecutionsHandler() {
        return new GetExecutionsHandler();
    }

    private static Future<JsonObject> createFuture(EventBus eventBus, JsonObject request, RequestType requestType,
                                                   String cookie, ResultHandler resultHandler) {
        Future<JsonObject> future = Future.future();
        request.put(COOKIE_STRING, cookie);

        eventBus.<JsonObject>send(requestType.name(), request, reply -> {
            if (reply.succeeded()) {
                future.complete(resultHandler.handle(request, reply.result().body()));
            } else {
                future.fail(reply.cause());
            }
        });
        return future;
    }

    interface ResultHandler {
        JsonObject handle(JsonObject requestObj, JsonObject result);
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
            EXECUTIONS.put(key, new JsonObject().put("status", ExecutionKey.ExecutionStatus.RUNNING));
            retrieveFuture
                    .compose(uc -> {
                        /**
                         *  Create parallel futures and execute all Urls in parallel with the request
                         */
                            //TODO: Add End point configurations.
//                        return CompositeFuture.all(uc.getJsonArray("endPoints").stream().map(endPoint->{
                            return createFuture(eventBus, uc, RequestType.EXECUTE_UC, cookie, (requestObj, result) -> requestObj.put("actual", result));
//                        }).collect(Collectors.toList()));
                    }).compose(uc -> createFuture(eventBus, uc, RequestType.MATCH_RESULTS, cookie, (reqObj, res) -> reqObj.put("matching", res.getJsonObject("matching")).put("finalStatus", res.getString("finalStatus"))))
                    .compose(matcherResult -> {
                        matcherResult.remove(COOKIE_STRING);
                        matcherResult.remove("id");
                        EXECUTIONS.get(key).put("status", ExecutionKey.ExecutionStatus.COMPLETED).put("executions", matcherResult);
                        onSuccess(request, matcherResult);
                    }, finalFuture.setHandler(handler -> {
                        if (handler.failed()) {
                            handler.cause().printStackTrace();
                            EXECUTIONS.get(key).put("status", ExecutionKey.ExecutionStatus.ABORT);
                            onFailure(request, handler.cause());
                        }
                    }));
        }
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

    static class GetExecutionsHandler extends AbstractRequestHandler<JsonObject, JsonArray> {
        GetExecutionsHandler() {
            super("criteria", RequestType.GET_ALL_EXECUTIONS);
        }

        @Override
        protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
            return body == null || body.toString().isEmpty() ? new JsonObject() : body.toJsonObject();
        }
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

        public enum ExecutionStatus {RUNNING, COMPLETED, FAIL, ABORT}
    }
}
