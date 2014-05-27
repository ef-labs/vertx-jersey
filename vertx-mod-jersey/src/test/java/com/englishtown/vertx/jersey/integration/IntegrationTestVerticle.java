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

import com.englishtown.vertx.jersey.JerseyModule;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 6/28/13
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntegrationTestVerticle extends TestVerticle {

    /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     */
    @Override
    public void start(final Future<Void> startedResult) {

        JsonObject config = loadConfig();

        container.deployVerticle(JerseyModule.class.getName(), config, new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> result) {
                if (result.succeeded()) {
                    startedResult.setResult(null);
                    IntegrationTestVerticle.super.start();
                } else {
                    startedResult.setFailure(result.cause());
                }

            }
        });

    }

    private JsonObject loadConfig() {

        try (InputStream stream = this.getClass().getResourceAsStream("/config.json")) {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = reader.readLine();
            }

            return new JsonObject(sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
            return new JsonObject();
        }

    }

    private HttpClient createHttpClient() {
        HttpClient client = vertx.createHttpClient();

        client.setHost("localhost")
                .setPort(8080)
                .setConnectTimeout(1000)
                .exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable event) {
                        fail();
                    }
                });

        return client;
    }

    private void verifyResponse(HttpClientResponse response, int status, String contentType, final String expected) {

        assertEquals(status, response.statusCode());
        assertEquals(contentType, response.headers().get(HttpHeaders.CONTENT_TYPE));

        if (expected != null) {
            response.bodyHandler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer body) {
                    String result = body.toString("UTF-8");
                    assertEquals(expected, result);
                    testComplete();
                }
            });
        } else {
            testComplete();
        }
    }

    @Test
    public void test_getJson() throws Exception {

        HttpClient client = createHttpClient();

        HttpClientRequest request = client.get("/rest/test/json",
                new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(HttpClientResponse response) {
                        verifyResponse(response, 200, MediaType.APPLICATION_JSON, "{}");
                    }
                }
        );

        request.end();
    }

    @Test
    public void test_getJsonp() throws Exception {

        HttpClient client = createHttpClient();
        final String contentType = "application/javascript";

        HttpClientRequest request = client.get("/rest/test/json?cb=foo_cb",
                new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(HttpClientResponse response) {
                        verifyResponse(response, 200, contentType, "foo_cb({})");
                    }
                }
        );

        request.headers().add(HttpHeaders.ACCEPT, contentType);
        request.end();
    }

    @Test
    public void test_getHtml() throws Exception {

        HttpClient client = createHttpClient();

        HttpClientRequest request = client.get("/rest/test/html",
                new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(HttpClientResponse response) {
                        verifyResponse(response, 200, MediaType.TEXT_HTML, null);
                    }
                }
        );

        request.end();
    }

    @Test
    public void test_postJson() throws Exception {

        HttpClient client = createHttpClient();

        HttpClientRequest request = client.post("/rest/test/json",
                new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(HttpClientResponse response) {
                        verifyResponse(response, 200, MediaType.APPLICATION_JSON, "{\"name\":\"async response\"}");
                    }
                }
        );

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end("{\"name\":\"post\"}");
    }

    @Test
    public void test_getChunked() throws Exception {

        HttpClient client = createHttpClient();

        HttpClientRequest request = client.get("/rest/test/chunked",
                new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(HttpClientResponse response) {
                        verifyResponse(response, 200, MediaType.TEXT_PLAIN, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    }
                }
        );

        request.end();
    }

}
