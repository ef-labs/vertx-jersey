package com.englishtown.vertx.jersey;


import com.englishtown.vertx.hk2.HK2VerticleFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Runner {
    private static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {
        //ClassLoader cl = Thread.currentThread().getContextClassLoader();
        vertx.registerVerticleFactory(new HK2VerticleFactory());
        JsonObject config = new JsonObject();
        config.put("resources", new JsonArray().add("com.englishtown.vertx.jersey"));
        config.put("port", 8000);
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(config);
        vertx.deployVerticle("java-hk2:" + JerseyModule.class.getCanonicalName(), options);

    }
}
