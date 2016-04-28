package com.englishtown.vertx.jersey.features.jackson;

import com.englishtown.vertx.jersey.features.jackson.internal.JsonArrayDeserializer;
import com.englishtown.vertx.jersey.features.jackson.internal.JsonArraySerializer;
import com.englishtown.vertx.jersey.features.jackson.internal.JsonObjectDeserializer;
import com.englishtown.vertx.jersey.features.jackson.internal.JsonObjectSerializer;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Jackson {@link Module} to register vert.x serializers
 */
public class VertxSerializerModule extends SimpleModule {

    public VertxSerializerModule() {
        super("et.vertx.serializers", Version.unknownVersion());
        init();
    }

    protected void init() {
        this.addSerializer(JsonObject.class, new JsonObjectSerializer())
                .addSerializer(JsonArray.class, new JsonArraySerializer())
                .addDeserializer(JsonObject.class, new JsonObjectDeserializer())
                .addDeserializer(JsonArray.class, new JsonArrayDeserializer());
    }
}
