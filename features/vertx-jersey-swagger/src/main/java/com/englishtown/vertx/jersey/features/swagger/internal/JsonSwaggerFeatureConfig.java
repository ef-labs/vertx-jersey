package com.englishtown.vertx.jersey.features.swagger.internal;

import com.englishtown.vertx.jersey.features.swagger.SwaggerFeatureConfig;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;

/**
 * Vert.x config implementation of {@link SwaggerFeatureConfig}
 */
public class JsonSwaggerFeatureConfig implements SwaggerFeatureConfig {

    private final JsonObject config;

    private static final String CONFIG_ENABLED = "enabled";

    @Inject
    public JsonSwaggerFeatureConfig(Vertx vertx) {
        this(getConfig(vertx));
    }

    public JsonSwaggerFeatureConfig(JsonObject config) {
        this.config = config;
    }

    @Override
    public boolean enabled() {
        return config.getBoolean(CONFIG_ENABLED, true);
    }

    private static JsonObject getConfig(Vertx vertx) {
        return vertx.getOrCreateContext()
                .config()
                .getJsonObject("jersey", new JsonObject())
                .getJsonObject("swagger", new JsonObject());
    }

}
