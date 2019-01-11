package com.rags.tools.uter.service;

import com.rags.tools.uter.RequestType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by ragha on 26-07-2018.
 */
public class UseCaseService {

    public static Handler<RoutingContext> createUCHandler() {
        return new CreationHandler("uc", RequestType.CREATE_UC);
    }

    public static Handler<RoutingContext> createUCSHandler() {
        return new CreationHandler("ucs", RequestType.CREATE_UCS);
    }

    public static Handler<RoutingContext> getUCHandler() {
        return new RetrievalHandler("id", RequestType.FETCH_UC);
    }

    public static Handler<RoutingContext> getUCSHandler() {
        return new RetrievalHandler("id", RequestType.FETCH_UCS);
    }

    public static Handler<RoutingContext> getAllUCHandler() {
        return new AbstractRequestHandler<JsonObject, JsonArray>("", RequestType.GET_ALL_UC) {
            @Override
            protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
                return new JsonObject();
            }
        };
    }

    static class CreationHandler extends AbstractRequestHandler<JsonObject, JsonObject> {
        CreationHandler(String key, RequestType requestType) {
            super(key, requestType);
        }

        @Override
        protected JsonObject getRequestData(HttpServerRequest request, Buffer body) {
            return body == null ? new JsonObject() : body.toJsonObject();
        }
    }

    static class RetrievalHandler extends AbstractRequestHandler<String, JsonObject> {
        RetrievalHandler(String key, RequestType requestType) {
            super(key, requestType);
        }

        @Override
        protected String getRequestData(HttpServerRequest request, Buffer body) {
            return request.getParam("id");
        }

    }
}