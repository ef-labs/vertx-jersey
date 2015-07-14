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

package com.englishtown.vertx.jersey.integration;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Integration tests for {@link com.englishtown.vertx.jersey.jackson.internal.JsonObjectSerializer} and
 * {@link com.englishtown.vertx.jersey.jackson.internal.JsonObjectDeserializer}.
 */
public class JsonObjectIntegrationTests extends JerseyHK2IntegrationTestBase {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    public static final String ENDPOINT = "/rest/test/object";

    private void verifyResponse(HttpClientResponse response, int status, String contentType, String expected) {

        assertEquals(status, response.statusCode());
        assertEquals(contentType, response.headers().get(HttpHeaders.CONTENT_TYPE));

        if (expected != null) {
            response.bodyHandler((Buffer body) -> {
                String result = body.toString("UTF-8");
                assertEquals(expected, result);
                testComplete();
            });
        } else {
            testComplete();
        }
    }

    @Test
    public void test_nullJsonObject() {

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 204, null, null);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end("null");
        await();

    }

    @Test
    public void test_invalidJsonObject() {

        String testJson = "{\"jsonObject\":{";

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 400, MediaType.TEXT_PLAIN, null);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end(testJson);
        await();

    }

    @Test
    public void test_jsonArrayWhereObjectExpected() {

        String testJson = "{\"jsonObject\":[]}";

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 400, MediaType.TEXT_PLAIN, null);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end(testJson);
        await();

    }

    @Test
    public void test_jsonStringWhereObjectExpected() {

        String testJson = "{\"jsonObject\":\"value\"}";

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 400, MediaType.TEXT_PLAIN, null);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end(testJson);
        await();

    }

    @Test
    public void test_emptyJsonObject() {

        String testJson = "{\"jsonObject\":{}}";

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, testJson);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end(testJson);
        await();

    }

    @Test
    public void test_simpleJsonObject() {

        String testJson = "{\"jsonObject\":{\"key\":\"value\"}}";

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, testJson);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end(testJson);
        await();

    }

    @Test
    public void test_complexJsonObject() {

        String testJson = "{\"jsonObject\":{\"innerObject\":{\"key\":\"value\"}}}";

        HttpClientRequest request = httpClient.request(HttpMethod.POST, PORT, HOST, ENDPOINT, response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, testJson);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end(testJson);
        await();

    }

}
