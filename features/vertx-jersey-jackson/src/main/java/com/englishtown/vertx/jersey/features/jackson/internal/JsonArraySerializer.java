package com.englishtown.vertx.jersey.features.jackson.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.vertx.core.json.JsonArray;

import java.io.IOException;

/**
 * Jackson serializer for Vert.x {@link JsonArray}
 */
public class JsonArraySerializer extends JsonSerializer<JsonArray> {
    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value       Value to serialize; can <b>not</b> be null.
     * @param gen         Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     */
    @Override
    public void serialize(JsonArray value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeObject(value.getList());
    }
}
