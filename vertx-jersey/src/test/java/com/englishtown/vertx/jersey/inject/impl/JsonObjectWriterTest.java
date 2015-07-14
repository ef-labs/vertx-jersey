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

package com.englishtown.vertx.jersey.inject.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.Assert.*;

/**
 * {@link JsonObjectWriter} tests.
 */
public class JsonObjectWriterTest {

    private JsonObjectWriter testWriter;
    private JsonObject testObject;

    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;

    @Before
    public void setUp() throws IOException {

        testWriter = new JsonObjectWriter();
        testObject = new JsonObject();

        inputStream = new PipedInputStream();
        outputStream = new PipedOutputStream(inputStream);

    }

    @After
    public void tearDown() throws IOException {

        outputStream.close();
        inputStream.close();

    }

    @Test
    public void test_isReadablePositive() {
        assertTrue(testWriter.isWriteable(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void test_isReadableNegative() {
        assertFalse(testWriter.isWriteable(JsonArray.class, null, null, MediaType.APPLICATION_JSON_TYPE));
        assertFalse(testWriter.isWriteable(String.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void test_getSize() {
        //Should always return -1 to have the length be auto-calculated
        assertEquals(-1, testWriter.getSize(null, null, null, null, null));
    }

    @Test
    public void test_readFromEmpty() throws IOException {

        testWriter.writeTo(testObject, JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, outputStream);
        outputStream.close();

        assertEquals("{}", ReaderWriter.readFromAsString(inputStream, MediaType.APPLICATION_JSON_TYPE));

    }

    @Test
    public void test_readFromSimple() throws IOException {

        testObject.put("key", "value");

        testWriter.writeTo(testObject, JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, outputStream);
        outputStream.close();

        assertEquals("{\"key\":\"value\"}",
                ReaderWriter.readFromAsString(inputStream, MediaType.APPLICATION_JSON_TYPE));

    }

    @Test
    public void test_readFromNestedArray() throws IOException {

        JsonArray innerArray = new JsonArray();
        innerArray.add("one");
        innerArray.add("two");
        innerArray.add("three");
        testObject.put("keywords", innerArray);

        testWriter.writeTo(testObject, JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, outputStream);
        outputStream.close();

        assertEquals("{\"keywords\":[\"one\",\"two\",\"three\"]}",
                ReaderWriter.readFromAsString(inputStream, MediaType.APPLICATION_JSON_TYPE));

    }

    @Test
    public void test_readFromNestedObject() throws IOException {

        JsonObject innerObject = new JsonObject();
        innerObject.put("key", "value");
        testObject.put("inner", innerObject);

        testWriter.writeTo(testObject, JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, outputStream);
        outputStream.close();

        assertEquals("{\"inner\":{\"key\":\"value\"}}",
                ReaderWriter.readFromAsString(inputStream, MediaType.APPLICATION_JSON_TYPE));

    }

}
