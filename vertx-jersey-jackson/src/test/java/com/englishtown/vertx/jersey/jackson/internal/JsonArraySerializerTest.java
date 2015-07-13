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
import io.vertx.core.json.JsonArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link JsonArraySerializer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonArraySerializerTest {

    @Captor
    private ArgumentCaptor<Object> valueCaptor;

    @Mock
    private JsonGenerator generator;

    private JsonArraySerializer serializer;

    @Before
    public void setUp() {

        serializer = new JsonArraySerializer();

    }

    @Test
    public void testSerializeEmptyArray() throws IOException {

        serializer.serialize(new JsonArray(), generator, null);

        verify(generator).writeObject(valueCaptor.capture());

        Object capturedObject = valueCaptor.getValue();

        assertTrue(capturedObject instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> capturedList = (List<Object>) capturedObject;

        assertTrue(capturedList.isEmpty());

    }

    @Test
    public void testSerialize() throws IOException {


        JsonArray testArray = new JsonArray();
        testArray.add("first");
        testArray.add("second");

        serializer.serialize(testArray, generator, null);

        verify(generator).writeObject(valueCaptor.capture());

        Object capturedObject = valueCaptor.getValue();

        assertTrue(capturedObject instanceof List);
        @SuppressWarnings("unchecked")
        List<Object> capturedList = (List<Object>) capturedObject;

        assertEquals(2, capturedList.size());
        assertTrue(capturedList.contains("first"));
        assertTrue(capturedList.contains("second"));

    }

}
