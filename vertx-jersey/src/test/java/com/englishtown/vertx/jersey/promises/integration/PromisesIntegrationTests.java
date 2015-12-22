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
import com.englishtown.vertx.guice.GuiceVertxBinder;
import com.englishtown.vertx.guice.WhenGuiceJerseyBinder;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.hk2.WhenHK2JerseyBinder;
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
import com.englishtown.vertx.jersey.promises.WhenJerseyServer;
import com.englishtown.vertx.jersey.promises.impl.DefaultWhenJerseyServer;
import com.englishtown.vertx.promises.guice.GuiceWhenBinder;
import com.englishtown.vertx.promises.hk2.HK2WhenBinder;
import com.google.inject.Injector;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration test for
 */
public abstract class PromisesIntegrationTests extends VertxTestBase {

    private String host = "localhost";
    private int port = 8080;
    private JerseyServer jerseyServer;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createServer(host, port);
    }

    @Override
    protected void tearDown() throws Exception {
        if (jerseyServer != null) {
            jerseyServer.close();
            jerseyServer = null;
        }
        super.tearDown();
    }

    private void createServer(String host, int port) throws Exception {

        JsonObject config = new JsonObject()
                .put("host", host)
                .put("port", port)
                .put("resources", new JsonArray().add("com.englishtown.vertx.jersey.promises.integration.resources"));

        CountDownLatch latch = new CountDownLatch(1);

        getWhenJerseyServer().createServer(config)
                .then(value -> {
                    jerseyServer = value;
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

    @Test
    public void testCreateMultipleServers() throws Exception {

        // create a second server on port2
        int port2 = 8081;
        createServer(host, port2);

        HttpClientOptions options = new HttpClientOptions().setConnectTimeout(300);

        vertx.createHttpClient(options)
                .request(HttpMethod.GET, port, host, "/integration/test", response -> {
                    assertEquals(200, response.statusCode());

                    vertx.createHttpClient(options)
                            .request(HttpMethod.GET, port2, host, "/integration/test", response2 -> {
                                assertEquals(200, response2.statusCode());
                                testComplete();
                            })
                            .exceptionHandler(t -> fail(t.getMessage()))
                            .end();
                })
                .exceptionHandler(t -> fail(t.getMessage()))
                .end();

        await();
    }

    protected abstract WhenJerseyServer getWhenJerseyServer();

    public static class Guice extends PromisesIntegrationTests {

        private Injector injector;

        @Override
        protected WhenJerseyServer getWhenJerseyServer() {
            if (injector == null) {
                injector = com.google.inject.Guice.createInjector(new WhenGuiceJerseyBinder(), new GuiceWhenBinder(), new GuiceVertxBinder(vertx));
            }
            return injector.getInstance(WhenJerseyServer.class);
        }

    }

    public static class HK2 extends PromisesIntegrationTests {

        private ServiceLocator locator;

        @Override
        protected WhenJerseyServer getWhenJerseyServer() {

            if (locator == null) {
                locator = ServiceLocatorUtilities.bind(new WhenHK2JerseyBinder(), new HK2WhenBinder(), new HK2VertxBinder(vertx));
            }

            return locator.getService(WhenJerseyServer.class);
        }

        @Override
        protected void tearDown() throws Exception {
            super.tearDown();
            if (locator != null) {
                locator.shutdown();
                locator = null;
            }
        }
    }

    public static class Simple extends PromisesIntegrationTests {

        @Override
        protected WhenJerseyServer getWhenJerseyServer() {

            ContainerResponseWriterProvider provider = new VertxResponseWriterProvider(
                    vertx,
                    new ArrayList<>(),
                    new ArrayList<>());

            VertxContainer container = new DefaultVertxContainer(vertx, null, null);
            JerseyHandler handler = new DefaultJerseyHandler(provider, new ArrayList<>());
            JerseyServer server = new DefaultJerseyServer(handler, container);
            JerseyOptions options = new DefaultJerseyOptions();
            When when = WhenFactory.createSync();

            return new DefaultWhenJerseyServer(vertx, () -> server, () -> options, when);

        }

    }
}