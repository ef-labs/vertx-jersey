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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import static org.junit.Assert.*;

/**
 * {@link JsonObjectReader} tests.
 */
public class JsonObjectReaderTest {

    private JsonObjectReader testReader;

    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;

    @Before
    public void setUp() throws IOException {

        testReader = new JsonObjectReader();

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
        assertTrue(testReader.isReadable(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void test_isReadableNegative() {
        assertFalse(testReader.isReadable(JsonArray.class, null, null, MediaType.APPLICATION_JSON_TYPE));
        assertFalse(testReader.isReadable(String.class, null, null, MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void test_readFromEmpty() throws IOException {

        outputStream.write("{}".getBytes());
        outputStream.close();

        JsonObject result = testReader.readFrom(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, inputStream);

        assertNotNull(result);
        assertEquals(0, result.size());

    }

    @Test
    public void test_readFromSimple() throws IOException {

        outputStream.write("{\"key\": \"value\"}".getBytes());
        outputStream.close();

        JsonObject result = testReader.readFrom(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, inputStream);

        assertNotNull(result);
        assertEquals("value", result.getString("key"));

    }

    @Test
    public void test_readFromNestedArray() throws IOException {

        outputStream.write("{\"keywords\": [\"one\", \"two\", \"three\"]}".getBytes());
        outputStream.close();

        JsonObject result = testReader.readFrom(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, inputStream);

        assertNotNull(result);
        JsonArray innerArray = result.getJsonArray("keywords");
        assertEquals(3, innerArray.size());

    }

    @Test
    public void test_readFromNestedObject() throws IOException {

        outputStream.write("{\"inner\": {\"key\": \"value\"}}".getBytes());
        outputStream.close();

        JsonObject result = testReader.readFrom(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, inputStream);

        assertNotNull(result);
        JsonObject innerObject = result.getJsonObject("inner");
        assertEquals("value", innerObject.getString("key"));

    }

    @Test(expected=BadRequestException.class)
    public void test_readFromInvalidInput() throws IOException {

        outputStream.write("{".getBytes());
        outputStream.close();

        //This should throw an exception
        testReader.readFrom(JsonObject.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, inputStream);

    }

}
