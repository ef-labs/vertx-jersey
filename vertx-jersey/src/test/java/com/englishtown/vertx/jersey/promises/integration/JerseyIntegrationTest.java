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

package com.englishtown.vertx.jersey.promises.integration;

import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.VertxContainer;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyOptions;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultVertxContainer;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.impl.VertxResponseWriterProvider;
import com.englishtown.vertx.jersey.promises.impl.DefaultWhenJerseyServer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration test for
 */
public class JerseyIntegrationTest extends VertxTestBase {

    private String host = "localhost";
    private int port = 8080;

    @Test
    public void testCreateServer() throws Exception {

        HttpClientOptions options = new HttpClientOptions().setConnectTimeout(300);

        vertx.createHttpClient(options)
                .request(HttpMethod.GET, port, host, "/integration/test", response -> {
                    assertEquals(200, response.statusCode());
                    testComplete();
                })
                .exceptionHandler(t -> fail(t.getMessage()))
                .end();

        await();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        ContainerResponseWriterProvider provider = new VertxResponseWriterProvider(
                vertx,
                new ArrayList<>(),
                new ArrayList<>());

        VertxContainer container = new DefaultVertxContainer(vertx, null, null);
        JerseyHandler handler = new DefaultJerseyHandler(provider, new ArrayList<>());
        JerseyServer server = new DefaultJerseyServer(handler, container);
        JerseyOptions options = new DefaultJerseyOptions();
        When when = WhenFactory.createSync();

        DefaultWhenJerseyServer whenJerseyServer = new DefaultWhenJerseyServer(vertx, server, options, when);

        JsonObject config = new JsonObject()
                .put("host", host)
                .put("port", port)
                .put("resources", new JsonArray().add("com.englishtown.vertx.jersey.promises.integration.resources"));

        CountDownLatch latch = new CountDownLatch(1);

        whenJerseyServer.createServer(config)
                .then(value -> {
                    latch.countDown();
                    return null;
                })
                .otherwise(t -> {
                    latch.countDown();
                    t.printStackTrace();
                    fail();
                    return null;
                });

        latch.await(10, TimeUnit.SECONDS);
    }
}