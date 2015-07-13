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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonObjectDeserializerTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonObjectDeserializerTest {

    private JsonObjectDeserializer deserializer;

    @Mock
    private JsonParser parser;

    @Mock
    private DeserializationContext context;

    @Before
    public void setUp() {
        deserializer = new JsonObjectDeserializer();
    }

    @Test
    public void testDeserializeEmptyObject() throws IOException {

        when(parser.readValueAs(any(TypeReference.class))).thenReturn(new LinkedHashMap<>());

        JsonObject deserializedObject = deserializer.deserialize(parser, context);

        assertNotNull(deserializedObject);
        assertEquals(0, deserializedObject.size());

    }

    @Test
    public void testDeserialize() throws IOException {

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("first", "1");
        results.put("second", "2");

        when(parser.readValueAs(any(TypeReference.class))).thenReturn(results);

        JsonObject deserializedObject = deserializer.deserialize(parser, context);

        assertNotNull(deserializedObject);
        assertEquals(2, deserializedObject.size());
        assertEquals("1", deserializedObject.getString("first"));
        assertEquals("2", deserializedObject.getString("second"));

    }

}