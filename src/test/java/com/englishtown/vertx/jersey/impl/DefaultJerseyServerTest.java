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
    JerseyConfigurator configurator;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;

    @Before
    public void setUp() {

        when(container.logger()).thenReturn(logger);
        when(vertx.createHttpServer()).thenReturn(httpServer);
        when(configurator.getVertx()).thenReturn(vertx);
        when(configurator.getContainer()).thenReturn(container);

        //noinspection unchecked
        Provider<JerseyHandler> handlerProvider = mock(Provider.class);
        when(handlerProvider.get()).thenReturn(jerseyHandler);
        jerseyServer = new DefaultJerseyServer(handlerProvider);

    }

    private void verifyResults(int port, String host) {

        verify(vertx, times(1)).createHttpServer();
        verify(jerseyHandler).init(any(JerseyConfigurator.class));
        verify(httpServer).requestHandler(jerseyHandler);
        //noinspection unchecked
        verify(httpServer, times(1)).listen(eq(port), eq(host), any(Handler.class));

    }

    @Test
    public void testConstructor_No_Handler() throws Exception {

        //noinspection unchecked
        Provider<JerseyHandler> provider = mock(Provider.class);

        try {
            new DefaultJerseyServer(provider);
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

        when(configurator.getHost()).thenReturn(host);
        when(configurator.getPort()).thenReturn(port);
        when(configurator.getReceiveBufferSize()).thenReturn(bufferSize);
        when(configurator.getVertx()).thenReturn(vertx);
        when(configurator.getContainer()).thenReturn(container);

        jerseyServer.init(configurator);
        verifyResults(port, host);

        verify(httpServer).setReceiveBufferSize(bufferSize);
    }

    @Test
    public void testInit_Listen_Result() throws Exception {

        jerseyServer.init(configurator, doneHandler);

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
        jerseyServer.init(configurator);

        verify(routeMatcherHandler).handle(any(RouteMatcher.class));

    }

}
