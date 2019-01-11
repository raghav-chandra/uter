package com.rags.tools.uter.vertical;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AbstractRestServiceVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRestServiceVerticle.class);

    private static final String COOKIE_STRING = "Cookie";

    void executeGetRequest(HttpClientOptions options, Message<JsonObject> message, String path, ResponseType responseType) {
        String cookie = message.body().getString(COOKIE_STRING);
        HttpClientRequest request = createRequest(options, message, path, HttpMethod.GET, responseType);
        request.headers().set("Content-Type", "application/json");
        if (cookie != null) {
            request.headers().set(COOKIE_STRING, cookie);
        }
        request.exceptionHandler(handler -> message.fail(7777, handler.getMessage()));
        request.end();
    }

    void executePostRequest(HttpClientOptions options, Message<JsonObject> message, String path, Object payload, ResponseType responseType) {
        String cookie = message.body().getString(COOKIE_STRING);
        HttpClientRequest request = createRequest(options, message, path, HttpMethod.POST, responseType);
        String data = Json.encode(payload);
        request.headers().set("Content-Type", "application/json").set("Content-Length", "" + data.length());
        if (cookie != null) {
            request.headers().set(COOKIE_STRING, cookie);
        }
        request.write(data);
        request.exceptionHandler(handler -> message.fail(7777, handler.getMessage()));
    }

    private HttpClientRequest createRequest(HttpClientOptions option, Message<JsonObject> message, String path, HttpMethod httpMethod, ResponseType responseType) {
        LOGGER.info("Executing {} call on API [{}://{}:{}/{}]", httpMethod, option.isSsl() ? "https" : "http", option.getDefaultHost(), option.getDefaultPort(), path);

        HttpClient client = vertx.createHttpClient(option);
        return client.request(httpMethod, path, response -> {
            LOGGER.info("Received response for {} call on API [{}://{}:{}/{}]", httpMethod, option.isSsl() ? "https" : "http", option.getDefaultHost(), option.getDefaultPort(), path);
            if (response.statusCode() == 200) {
                response.bodyHandler(data -> {
                    switch (responseType) {
                        case OBJECT:
                            message.reply(data.toJsonObject());
                            break;
                        case ARRAY:
                            message.reply(data.toJsonArray());
                            break;
                        case TEXT:
                            message.reply(data.toString());
                            break;
                        default:
                            message.fail(8888, "Expected response type not matching with actual");
                    }
                    client.close();
                });
            } else {
                response.bodyHandler(data -> {
                    message.fail(9999, data.toString());
                    client.close();
                });
            }
        });
    }

    HttpClientOptions createClientOptions(Boolean ssl, String host, Integer port) {
        return new HttpClientOptions().setSsl(ssl).setDefaultHost(host).setDefaultPort(port);
    }

    enum ResponseType {
        OBJECT, ARRAY, TEXT
    }
}
