package com.englishtown.vertx.jersey.features.jackson.internal;

import com.englishtown.vertx.jersey.features.jackson.ObjectMapperConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.*;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.*;

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

        if (SnakeCaseStrategy.class.getSimpleName().equalsIgnoreCase(s)
                || LowerCaseWithUnderscoresStrategy.class.getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(SNAKE_CASE);
            return;
        }

        if (LowerCaseStrategy.class.getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(LOWER_CASE);
            return;
        }

        if (UpperCamelCaseStrategy.class.getSimpleName().equalsIgnoreCase(s)
                || PascalCaseStrategy.class.getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(UPPER_CAMEL_CASE);
            return;
        }

        if (KebabCaseStrategy.class.getSimpleName().equalsIgnoreCase(s)) {
            mapper.setPropertyNamingStrategy(KEBAB_CASE);
            return;
        }

        throw new IllegalArgumentException("Property naming strategy " + s + " is not supported");

    }

}
