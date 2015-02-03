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

package com.englishtown.vertx.jersey.promises.impl;

import com.englishtown.promises.Done;
import com.englishtown.promises.When;
import com.englishtown.promises.WhenFactory;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyServer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.inject.Provider;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultWhenJerseyServer}
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultWhenJerseyServerTest {

    private DefaultWhenJerseyServer whenJerseyServer;
    JsonObject config = new JsonObject();
    private Done<JerseyServer> done = new Done<>();

    @Mock
    JerseyServer server;
    @Mock
    JerseyOptions options;
    @Mock
    Vertx vertx;
    @Mock
    AsyncResult<HttpServer> result;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;

    @Before
    public void setUp() {
        When when = WhenFactory.createSync();
        whenJerseyServer = new DefaultWhenJerseyServer(vertx, server, options, when);
    }

    @Test
    public void testCreateServer_Success() throws Exception {

        when(result.succeeded()).thenReturn(true);

        whenJerseyServer.createServer(config)
                .then(done.onFulfilled, done.onRejected);

        verify(options).init(eq(config), eq(vertx));
        verify(server).init(eq(options), handlerCaptor.capture());

        handlerCaptor.getValue().handle(result);
        done.assertFulfilled();

    }

    @Test
    public void testCreateServer_Fail() throws Exception {

        when(result.succeeded()).thenReturn(false);

        whenJerseyServer.createServer(config)
                .then(done.onFulfilled, done.onRejected);

        verify(options).init(eq(config), eq(vertx));
        verify(server).init(eq(options), handlerCaptor.capture());

        handlerCaptor.getValue().handle(result);
        done.assertRejected();

    }

}
