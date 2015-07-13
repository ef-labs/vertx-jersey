/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey.jackson.internal;

import com.englishtown.vertx.jersey.jackson.ObjectMapperConfigurator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.ws.rs.ext.ContextResolver;

/**
 * A custom ContextResolver that returns a Jackson ObjectMapper that will automatically deserialize JsonObjects when
 * appropriate.
 */
public class JsonObjectMapperProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    @Inject
    public JsonObjectMapperProvider(ObjectMapperConfigurator configurator) {

        mapper = new ObjectMapper();

        SimpleModule jsonObjectModule = new SimpleModule("JsonObjectDeserializerModule", Version.unknownVersion())
            .addSerializer(JsonArray.class, new JsonArraySerializer())
            .addSerializer(JsonObject.class, new JsonObjectSerializer())
            .addDeserializer(JsonArray.class, new JsonArrayDeserializer())
            .addDeserializer(JsonObject.class, new JsonObjectDeserializer());

        mapper.registerModule(jsonObjectModule);

        configurator.configure(mapper);

    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

}
