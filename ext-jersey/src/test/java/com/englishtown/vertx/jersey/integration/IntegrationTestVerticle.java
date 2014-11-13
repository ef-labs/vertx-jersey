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
import io.vertx.core.DeploymentOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 */
public class IntegrationTestVerticle extends VertxTestBase {

    private String host = "localhost";
    private int port = 8080;

    private HttpClient client;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        JsonObject config = loadConfig();
        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);
        CountDownLatch latch = new CountDownLatch(1);

        vertx.deployVerticle("java-hk2:" + JerseyModule.class.getName(), deploymentOptions, result -> {
            latch.countDown();
            if (!result.succeeded()) {
                result.cause().printStackTrace();
                fail();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setConnectTimeout(1000);

        client = vertx.createHttpClient(clientOptions)
                .exceptionHandler(t -> fail(t.getMessage()));

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

    private void verifyResponse(HttpClientResponse response, int status, String contentType, final String expected) {

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
    public void test_getJson() throws Exception {

        HttpClientRequest request = client.request(HttpMethod.GET, port, host, "/rest/test/json", response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, "{}");
        });

        request.end();
        await();
    }

    @Test
    public void test_getJsonp() throws Exception {

        final String contentType = "application/javascript";

        HttpClientRequest request = client.request(HttpMethod.GET, port, host, "/rest/test/json?cb=foo_cb", response -> {
            verifyResponse(response, 200, contentType, "foo_cb({})");
        });

        request.headers().add(HttpHeaders.ACCEPT, contentType);
        request.end();
        await();
    }

    @Test
    public void test_getHtml() throws Exception {

        HttpClientRequest request = client.request(HttpMethod.GET, port, host, "/rest/test/html", response -> {
            verifyResponse(response, 200, MediaType.TEXT_HTML, null);
        });

        request.end();
        await();
    }

    @Test
    public void test_postJson() throws Exception {

        HttpClientRequest request = client.request(HttpMethod.POST, port, host, "/rest/test/json", response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, "{\"name\":\"async response\"}");
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end("{\"name\":\"post\"}");
        await();
    }

    @Test
    public void test_getChunked() throws Exception {

        HttpClientRequest request = client.request(HttpMethod.GET, port, host, "/rest/test/chunked", response -> {
            verifyResponse(response, 200, MediaType.TEXT_PLAIN, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        });

        request.end();
        await();
    }

}
