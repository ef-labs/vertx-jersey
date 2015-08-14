package com.englishtown.vertx.jersey.features.jackson.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.vertx.core.json.JsonArray;

import java.io.IOException;
import java.util.List;

/**
 * Jackson deserializer for Vert.x {@link JsonArray}
 */
public class JsonArrayDeserializer extends JsonDeserializer<JsonArray> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonArray deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        List list = p.readValueAs(List.class);
        return new JsonArray(list);
    }
}
