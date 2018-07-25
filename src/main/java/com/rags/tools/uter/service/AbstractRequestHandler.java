package com.rags.tools.uter.service;

import com.rags.tools.uter.RequestType;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by ragha on 22-04-2018.
 */
public abstract class AbstractRequestHandler<S, T> implements Handler<RoutingContext> {

    private final String key;
    private final RequestType requestType;

    public AbstractRequestHandler(String key, RequestType requestType) {
        this.key = key;
        this.requestType = requestType;
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest request = context.request();
        EventBus eventBus = context.vertx().eventBus();

        S requestData = getRequestData(request, context.getBody());
        JsonObject reqObject = createRequestObject(key, requestData);

        handleFuture(request, requestData, createFuture(requestType, eventBus, reqObject), eventBus);

    }

    protected void handleFuture(HttpServerRequest request, S requestData, Future<T> future, EventBus eventBus) {
        future.setHandler(handler -> {
            if (handler.succeeded()) {
                onSuccess(request, handler.result());
            } else {
                onFailure(request, handler.cause());
            }
        });
    }

    private void onSuccess(HttpServerRequest request, T result) {
        request.response().end(JsonUtil.createSuccessResponse(result).encodePrettily());
    }

    private void onFailure(HttpServerRequest request, Throwable cause) {
        request.response().end(JsonUtil.createFailedResponse(cause.getMessage()).encodePrettily());
    }

    private Future<T> createFuture(RequestType requestType, EventBus eventBus, JsonObject reqObject) {
        Future<T> future = Future.future();
        eventBus.<T>send(requestType.name(), reqObject, reply -> {
            if (reply.succeeded()) {
                future.complete(reply.result().body());
            } else {
                future.fail(reply.cause());
            }
        });
        return future;
    }

    protected JsonObject createRequestObject(String key, S requestData) {
        return new JsonObject().put(key, requestData);
    }

    protected abstract S getRequestData(HttpServerRequest request, Buffer body);
}
