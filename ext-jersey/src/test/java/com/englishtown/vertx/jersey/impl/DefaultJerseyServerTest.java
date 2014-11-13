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

import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.routematcher.RouteMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;
import java.net.URI;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link com.englishtown.vertx.jersey.JerseyModule} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyServerTest {

    DefaultJerseyServer jerseyServer;
    URI baseUri = URI.create("http://test.englishtown.com/test");

    @Mock
    Vertx vertx;
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
    Provider<JerseyHandler> jerseyHandlerProvider;
    @Mock
    JerseyConfigurator configurator;
    @Mock
    Handler<HttpServer> setupHandler;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;
    @Captor
    ArgumentCaptor<Handler<HttpServerRequest>> requestHandlerCaptor;
    @Captor
    ArgumentCaptor<HttpServerOptions> optionsCaptor;

    @Before
    public void setUp() {

        when(vertx.createHttpServer(any(HttpServerOptions.class))).thenReturn(httpServer);
        when(configurator.getVertx()).thenReturn(vertx);

        when(jerseyHandler.getBaseUri()).thenReturn(baseUri);
        when(jerseyHandlerProvider.get()).thenReturn(jerseyHandler);
        jerseyServer = new DefaultJerseyServer(jerseyHandlerProvider);

    }

    private void verifyResults(int port, String host) {

        verify(vertx).createHttpServer(optionsCaptor.capture());
        verify(jerseyHandler).init(any(JerseyConfigurator.class));

        verify(httpServer).requestHandler(requestHandlerCaptor.capture());
        Handler<HttpServerRequest> handler = requestHandlerCaptor.getValue();
        assertNotNull(handler);

        verify(httpServer).listen(any());

        HttpServerOptions options = optionsCaptor.getValue();
        assertEquals(port, options.getPort());
        assertEquals(host, options.getHost());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit_No_Handler() throws Exception {

        try {
            reset(jerseyHandlerProvider);
            new DefaultJerseyServer(jerseyHandlerProvider).init(configurator);
            fail();
        } catch (IllegalStateException e) {
            // Expected
            assertEquals("The JerseyHandler provider returned null", e.getMessage());
        }

    }

    @Test
    public void testInit_Default_Config() throws Exception {
        when(configurator.getHost()).thenReturn("0.0.0.0");
        when(configurator.getPort()).thenReturn(80);
        jerseyServer.init(configurator);
        verifyResults(80, "0.0.0.0");
    }

    @Test
    public void testInit_With_Config() throws Exception {

        String host = "test.englishtown.com";
        int port = 8888;
        int bufferSize = 1024;
        boolean ssl = true;

        when(configurator.getHost()).thenReturn(host);
        when(configurator.getPort()).thenReturn(port);
        when(configurator.getReceiveBufferSize()).thenReturn(bufferSize);
        when(configurator.getVertx()).thenReturn(vertx);
        when(configurator.getSSL()).thenReturn(ssl);

        jerseyServer.init(configurator);
        verifyResults(port, host);

        verify(vertx).createHttpServer(optionsCaptor.capture());

        HttpServerOptions options = optionsCaptor.getValue();

        assertEquals(host, options.getHost());
        assertEquals(port, options.getPort());
        assertEquals(bufferSize, options.getReceiveBufferSize());
        assertEquals(ssl, options.isSsl());

    }

    @Test
    public void testInit_Listen_Result() throws Exception {

        jerseyServer.init(configurator, doneHandler);

        verify(httpServer).listen(handlerCaptor.capture());
        Handler<AsyncResult<HttpServer>> handler = handlerCaptor.getValue();

        when(asyncResult.succeeded()).thenReturn(true).thenReturn(false);

        handler.handle(asyncResult);
//        verify(logger).info(anyString());
//        verify(logger, times(0)).error(anyString());

        handler.handle(asyncResult);
//        verify(logger).info(anyString());
//        verify(logger).error(anyString(), any(Throwable.class));

        verify(doneHandler, times(2)).handle(eq(asyncResult));
    }

    @Test
    public void testInit_Route_Match_Handler() throws Exception {

        URI uri = URI.create("/test/");
        when(jerseyHandler.getBaseUri()).thenReturn(uri);

        jerseyServer.routeMatcherHandler(routeMatcherHandler);
        jerseyServer.init(configurator);

        verify(routeMatcherHandler).handle(any(RouteMatcher.class));

    }

    @Test
    public void testGetHandler() throws Exception {
        jerseyServer.init(configurator);
        JerseyHandler handler = jerseyServer.getHandler();
        assertEquals(jerseyHandler, handler);
    }

    @Test
    public void testSetupHandler() throws Exception {
        jerseyServer.setupHandler(setupHandler);
        jerseyServer.init(configurator);
        verify(setupHandler).handle(eq(httpServer));
    }

    @Test
    public void testGetHttpServer() throws Exception {
        jerseyServer.init(configurator);
        HttpServer server = jerseyServer.getHttpServer();
        assertEquals(httpServer, server);
    }

}
