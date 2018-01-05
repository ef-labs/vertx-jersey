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
import com.englishtown.vertx.jersey.JerseyServerOptions;
import com.englishtown.vertx.jersey.VertxContainer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import org.glassfish.jersey.server.ApplicationHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link com.englishtown.vertx.jersey.JerseyVerticle} unit tests
 */
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
    JerseyServerOptions options;
    @Mock
    HttpServerOptions serverOptions;
    @Mock
    VertxContainer container;
    @Mock
    Handler<HttpServer> setupHandler;
    @Mock
    Future<Object> future;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;
    @Captor
    ArgumentCaptor<Handler<HttpServerRequest>> requestHandlerCaptor;
    @Captor
    ArgumentCaptor<HttpServerOptions> optionsCaptor;
    @Captor
    ArgumentCaptor<Handler<Future<Object>>> futureHandlerCaptor;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<Object>>> resultHandlerCaptor;

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {

        when(vertx.createHttpServer(any(HttpServerOptions.class))).thenReturn(httpServer);
        when(container.getVertx()).thenReturn(vertx);
        when(container.getApplicationHandler()).thenReturn(new ApplicationHandler());
        when(options.getServerOptions()).thenReturn(serverOptions);

        when(jerseyHandler.getBaseUri()).thenReturn(baseUri);
        jerseyServer = new DefaultJerseyServer(jerseyHandler, container, () -> options);

    }

    private void verifyResults(int port, String host) {

        verify(vertx).createHttpServer(optionsCaptor.capture());

        verify(httpServer).requestHandler(requestHandlerCaptor.capture());
        Handler<HttpServerRequest> handler = requestHandlerCaptor.getValue();
        assertNotNull(handler);

        verify(httpServer).listen(Mockito.any());

        HttpServerOptions options = optionsCaptor.getValue();
        assertEquals(port, options.getPort());
        assertEquals(host, options.getHost());

    }

    @Test
    public void testInit_Default_Config() throws Exception {
        when(serverOptions.getHost()).thenReturn("0.0.0.0");
        when(serverOptions.getPort()).thenReturn(80);
        jerseyServer.start();
        verifyExecuteBlocking();
        verifyResults(80, "0.0.0.0");
    }

    @Test
    public void testInit_With_Config() throws Exception {

        String host = "test.englishtown.com";
        int port = 8888;
        int bufferSize = 1024;
        boolean ssl = true;
        boolean compressionSupported = true;

        when(serverOptions.getHost()).thenReturn(host);
        when(serverOptions.getPort()).thenReturn(port);
        when(serverOptions.getReceiveBufferSize()).thenReturn(bufferSize);
        when(serverOptions.isSsl()).thenReturn(ssl);
        when(serverOptions.isCompressionSupported()).thenReturn(compressionSupported);

        jerseyServer.start();
        verifyExecuteBlocking();
        verifyResults(port, host);

        verify(vertx).createHttpServer(optionsCaptor.capture());

        HttpServerOptions options = optionsCaptor.getValue();

        assertEquals(host, options.getHost());
        assertEquals(port, options.getPort());
        assertEquals(bufferSize, options.getReceiveBufferSize());
        assertEquals(ssl, options.isSsl());
        assertEquals(compressionSupported, options.isCompressionSupported());

    }

    @Test
    public void testInit_Listen_Result() throws Exception {

        jerseyServer.start(doneHandler);
        verifyExecuteBlocking();

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
    public void testGetHandler() throws Exception {
        jerseyServer.start();
        JerseyHandler handler = jerseyServer.getHandler();
        assertEquals(jerseyHandler, handler);
    }

    @Test
    public void testSetupHandler() throws Exception {
        jerseyServer.setupHandler(setupHandler);
        jerseyServer.start();
        verify(setupHandler).handle(eq(httpServer));

        Handler<HttpServerRequest> requestHandler = jerseyServer.getHandler();
        assertNotNull(requestHandler);

        // Wrap the request with a custom route
        httpServer.requestHandler(request -> {
        });

    }

    @Test
    public void testGetHttpServer() throws Exception {
        jerseyServer.start();
        HttpServer server = jerseyServer.getHttpServer();
        assertEquals(httpServer, server);
    }

    private void verifyExecuteBlocking() {

        verify(vertx).executeBlocking(futureHandlerCaptor.capture(), resultHandlerCaptor.capture());

        futureHandlerCaptor.getValue().handle(future);
        verify(future).complete();

        resultHandlerCaptor.getValue().handle(future);

    }

}
