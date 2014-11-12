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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.ArrayList;

import javax.inject.Provider;

import org.junit.Test;

import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultJerseyConfigurator;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServer;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import com.englishtown.vertx.jersey.inject.impl.VertxResponseWriterProvider;
import com.englishtown.vertx.jersey.promises.impl.DefaultWhenJerseyServer;

/**
 * Integration test for
 */
public class JerseyIntegrationTest extends VertxTestBase {

    private static String host = "localhost";
    private static int port = 8080;

    @Test
    public void testCreateServer() throws Exception {

        HttpClientOptions options = new HttpClientOptions();
        options.setConnectTimeout(300);
        vertx.createHttpClient(options)
                .exceptionHandler(t -> fail(t.getMessage()))
                .request(HttpMethod.GET, port, host, "/integration/test", response -> {
                    assertEquals(200, response.statusCode());
                    testComplete();
                })
                .exceptionHandler(t -> fail(t.getMessage()))
                .end();

    }

}

class JerseyIntegrationTestImpl extends AbstractVerticle {
    
    String host = "localhost";
    int port = 8080;

    /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     */
    @SuppressWarnings("unchecked")
    @Override
    public void start(final Future<Void> startedResult) {

        ContainerResponseWriterProvider provider = new VertxResponseWriterProvider(
                getVertx(),
                new ArrayList<VertxResponseProcessor>(),
                new ArrayList<VertxPostResponseProcessor>());

        JerseyHandler handler = new DefaultJerseyHandler(provider, new ArrayList<VertxRequestProcessor>());
        Provider<JerseyHandler> handlerProvider = mock(Provider.class);
        when(handlerProvider.get()).thenReturn(handler);

        JerseyServer server = new DefaultJerseyServer(handlerProvider);
        Provider<JerseyServer> serverProvider = mock(Provider.class);
        when(serverProvider.get()).thenReturn(server);

        JerseyConfigurator configurator = new DefaultJerseyConfigurator(null);
        Provider<JerseyConfigurator> configuratorProvider = mock(Provider.class);
        when(configuratorProvider.get()).thenReturn(configurator);
        When when = WhenFactory.createSync();

        DefaultWhenJerseyServer whenJerseyServer = new DefaultWhenJerseyServer(vertx, serverProvider, configuratorProvider, when);

        JsonObject config = new JsonObject()
                .put("host", host)
                .put("port", port)
                .put("resources", new JsonArray().add("com.englishtown.vertx.jersey.promises.integration.resources"));

        whenJerseyServer.createServer(config).then(
                value -> {
//                    start();
                    startedResult.complete();
                    return null;
                },
                value -> {
                    startedResult.fail(value);
                    return null;
                }
        );

    }
}