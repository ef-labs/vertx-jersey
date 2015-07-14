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

import com.fasterxml.jackson.core.JsonGenerator;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link JsonObjectSerializer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonObjectSerializerTest {

    @Captor
    private ArgumentCaptor<Object> valueCaptor;

    @Mock
    private JsonGenerator generator;

    private JsonObjectSerializer serializer;

    @Before
    public void setUp() {

        serializer = new JsonObjectSerializer();

    }

    @Test
    public void testSerializeEmptyObject() throws IOException {

        serializer.serialize(new JsonObject(), generator, null);

        verify(generator).writeObject(valueCaptor.capture());

        Object capturedObject = valueCaptor.getValue();

        assertTrue(capturedObject instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> capturedMap = (Map<String, Object>) capturedObject;

        assertTrue(capturedMap.isEmpty());

    }

    @Test
    public void testSerialize() throws IOException {


        JsonObject testArray = new JsonObject();
        testArray.put("first", "1");
        testArray.put("second", "2");

        serializer.serialize(testArray, generator, null);

        verify(generator).writeObject(valueCaptor.capture());

        Object capturedObject = valueCaptor.getValue();

        assertTrue(capturedObject instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> capturedMap = (Map<String, Object>) capturedObject;

        assertEquals(2, capturedMap.size());
        assertEquals("1", capturedMap.get("first"));
        assertEquals("2", capturedMap.get("second"));

    }

}
