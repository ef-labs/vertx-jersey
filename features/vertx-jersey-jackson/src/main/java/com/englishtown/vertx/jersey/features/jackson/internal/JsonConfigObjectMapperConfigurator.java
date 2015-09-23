package com.englishtown.vertx.jersey.features.jackson.internal;

import com.englishtown.vertx.jersey.features.jackson.ObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;

/**
 * Vert.x json config implementation of {@link ObjectMapperConfigurator}
 */
public class JsonConfigObjectMapperConfigurator implements ObjectMapperConfigurator {

    private JsonObject config;

    public static final String CONFIG_PROPERTY_NAMING_STRATEGY = "property_naming_strategy";

    @Inject
    public JsonConfigObjectMapperConfigurator(Vertx vertx) {
        config = vertx.getOrCreateContext().config();
        config = config.getJsonObject("jersey", config).getJsonObject("jackson", config);
    }

    @Override
    public void configure(ObjectMapper mapper) {

        setPropertyNamingStrategy(mapper);

    }

    protected void setPropertyNamingStrategy(ObjectMapper mapper) {

        String s = config.getString(CONFIG_PROPERTY_NAMING_STRATEGY);

        if (s == null) {
            return;
        }

        if (PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES.getClass().getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            return;
        }

        if (PropertyNamingStrategy.LOWER_CASE.getClass().getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);
            return;
        }

        if (PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE.getClass().getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.PASCAL_CASE_TO_CAMEL_CASE);
            return;
        }

    }

}
