package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.promises.WhenHttpClient;
import com.englishtown.vertx.promises.WhenVertx;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * DI helper for integration tests
 */
public interface TestServiceLocator {

    void init(Vertx vertx);

    void tearDown();

    default JsonObject getConfig() {
        return ConfigUtils.loadConfig();
    }

    <T> T getService(Class<T> clazz);

    default WhenHttpClient getWhenHttpClient() {
        return getService(WhenHttpClient.class);
    }

    default WhenVertx getWhenVertx() {
        return getService(WhenVertx.class);
    }

    Promise<String> deployJerseyVerticle();

    default Promise<String> deployJerseyVerticle(String identifier) {
        DeploymentOptions options = new DeploymentOptions().setConfig(getConfig());
        return getWhenVertx().deployVerticle(identifier, options);
    }

}
