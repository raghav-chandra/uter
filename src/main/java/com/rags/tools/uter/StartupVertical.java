package com.rags.tools.uter;

import com.rags.tools.uter.service.ExecutionService;
import com.rags.tools.uter.service.UseCaseService;
import com.rags.tools.uter.verticle.ExecutionVerticle;
import com.rags.tools.uter.verticle.MatcherVerticle;
import com.rags.tools.uter.verticle.UseCaseVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ragha on 22-04-2018.
 */
public class StartupVertical extends AbstractVerticle {
    private static final String WEB_ROOT = "web.root";
    private static final String WEB_PORT = "web.port";
    private static final List<Class<? extends AbstractVerticle>> VERTICLES = Arrays.asList(UseCaseVerticle.class, ExecutionVerticle.class, MatcherVerticle.class);

    @Override
    public void start() {
        JsonObject config = config();
        Router router = Router.router(vertx);
        VERTICLES.forEach(verticle ->
                vertx.deployVerticle(verticle, new DeploymentOptions().setConfig(config), handler -> {
                    if (handler.succeeded()) {
                        System.out.println("Deployed " + verticle.getName());
                    } else {
                        handler.cause().printStackTrace();
                    }
                }));

        router.route().handler(BodyHandler.create());
        router.post("/futor/uc/create").handler(UseCaseService.createUCHandler());
        router.post("/futor/ucs/create").handler(UseCaseService.createUCSHandler());

        router.get("/futor/ucs/:id").handler(UseCaseService.createUCSHandler());
        router.get("/futor/uc/:id").handler(UseCaseService.createUCSHandler());
        router.post("/futor/uc/lookup").handler(UseCaseService.createUCLookupHandler());

        router.get("/futor/all").handler(UseCaseService.getAllUCHandler());
        router.get("/futor/executions/all").handler(ExecutionService.getExecutionsHandler());
        router.get("/futor/execute/uc/:id").handler(ExecutionService.ucExecutionHandler());

        router.route().handler(StaticHandler.create(config().getString(WEB_ROOT)));
        HttpServer server = vertx.createHttpServer().requestHandler(router::accept);
        server.listen(config().getInteger(WEB_PORT));
    }
}