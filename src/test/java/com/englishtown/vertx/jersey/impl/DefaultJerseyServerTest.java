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

package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.JerseyHandler;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import javax.inject.Provider;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * {@link com.englishtown.vertx.jersey.JerseyModule} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyServerTest {

    DefaultJerseyServer jerseyServer;

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    Logger logger;
    @Mock
    HttpServer httpServer;
    @Mock
    AsyncResult<HttpServer> asyncResult;
    @Mock
    JerseyHandler jerseyHandler;
    @Mock
    Handler<AsyncResult<HttpServer>> doneHandler;
    @Mock
    Handler<RouteMatcher> routeMatcherHandler;
    @Mock
    Factory<Ref<Vertx>> vertxReferenceFactory;
    @Mock
    Factory<Ref<Container>> containerReferenceFactory;
    @Mock
    Ref<Vertx> vertxReference;
    @Mock
    Ref<Container> containerReference;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;

    @Before
    public void setUp() {

        when(container.logger()).thenReturn(logger);
        when(vertx.createHttpServer()).thenReturn(httpServer);

        when(vertxReferenceFactory.provide()).thenReturn(vertxReference);
        when(containerReferenceFactory.provide()).thenReturn(containerReference);

        //noinspection unchecked
        Provider<JerseyHandler> provider = mock(Provider.class);
        when(provider.get()).thenReturn(jerseyHandler);
        jerseyServer = new DefaultJerseyServer(provider, vertxReferenceFactory, containerReferenceFactory);

    }

    private void verifyResults(int port, String host) {

        verify(vertx, times(1)).createHttpServer();
        verify(jerseyHandler).init(vertx, container);
        verify(httpServer).requestHandler(jerseyHandler);
        //noinspection unchecked
        verify(httpServer, times(1)).listen(eq(port), eq(host), any(Handler.class));

    }

    @Test
    public void testConstructor_No_Handler() throws Exception {

        //noinspection unchecked
        Provider<JerseyHandler> provider = mock(Provider.class);

        try {
            new DefaultJerseyServer(provider, null, null);
            fail();
        } catch (IllegalStateException e) {
            // Expected
            assertEquals("The JerseyHandler provider returned null", e.getMessage());
        }

    }

    @Test
    public void testInit_Default_Config() throws Exception {
        jerseyServer.init(new JsonObject(), vertx, container);
        verifyResults(80, "0.0.0.0");
    }

    @Test
    public void testInit_With_Config() throws Exception {

        String host = "test.englishtown.com";
        int port = 8888;
        int bufferSize = 1024;

        JsonObject config = new JsonObject()
                .putString(DefaultJerseyServer.CONFIG_HOST, host)
                .putNumber(DefaultJerseyServer.CONFIG_PORT, port)
                .putNumber(DefaultJerseyServer.CONFIG_RECEIVE_BUFFER_SIZE, bufferSize);

        jerseyServer.init(config, vertx, container);
        verifyResults(port, host);

        verify(httpServer).setReceiveBufferSize(bufferSize);
    }

    @Test
    public void testInit_Listen_Result() throws Exception {

        jerseyServer.init(new JsonObject(), vertx, container, doneHandler);

        verify(httpServer).listen(anyInt(), anyString(), handlerCaptor.capture());
        Handler<AsyncResult<HttpServer>> handler = handlerCaptor.getValue();

        when(asyncResult.succeeded()).thenReturn(true).thenReturn(false);

        handler.handle(asyncResult);
        verify(logger).info(anyString());
        verify(logger, times(0)).error(anyString());

        handler.handle(asyncResult);
        verify(logger).info(anyString());
        verify(logger).error(anyString(), any(Throwable.class));

        verify(doneHandler, times(2)).handle(eq(asyncResult));
    }

    @Test
    public void testInit_Route_Match_Handler() throws Exception {

        URI uri = URI.create("/test/");
        when(jerseyHandler.getBaseUri()).thenReturn(uri);

        jerseyServer.routeMatcherHandler(routeMatcherHandler);
        jerseyServer.init(new JsonObject(), vertx, container);

        verify(routeMatcherHandler).handle(any(RouteMatcher.class));

    }

}
