package com.rags.tools.uter;

import com.rags.tools.uter.service.UseCaseService;
import com.rags.tools.uter.vertical.UseCaseVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

/**
 * Created by ragha on 22-04-2018.
 */
public class StartupVertical extends AbstractVerticle {
    private static final String WEB_ROOT = "web.root";

    @Override
    public void start() throws Exception {
        JsonObject config = config();

        Router router = Router.router(vertx);

        vertx.deployVerticle(UseCaseVerticle.class, new DeploymentOptions().setConfig(config), handler -> {
            System.out.println("Deployed " + UseCaseVerticle.class.getName());
        });

        router.post("/uter/uc/create").handler(UseCaseService.createUCHandler());
        router.post("/uter/ucs/create").handler(UseCaseService.createUCSHandler());
    }
}
