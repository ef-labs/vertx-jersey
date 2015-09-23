package com.englishtown.vertx.jersey.features.jackson.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jackson deserializer for Vert.x {@link JsonObject}
 */
public class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {

    private final TypeReference<LinkedHashMap<String, Object>> type = new TypeReference<LinkedHashMap<String, Object>>() {
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Map<String, Object> map = p.readValueAs(type);
        return new JsonObject(map);
    }
}
