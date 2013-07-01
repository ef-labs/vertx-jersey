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

package com.englishtown.vertx.jersey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import javax.inject.Provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * {@link JerseyModule} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class JerseyModuleTest {

    JerseyModule jerseyModule;

    @Mock
    JerseyServer jerseyServer;
    @Mock
    JerseyServerFactory jerseyServerFactory;
    @Mock
    Future<Void> startedResult;
    @Mock
    AsyncResult<HttpServer> asyncResult;
    @Mock
    Container container;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;

    @Before
    public void setUp() {
        when(jerseyServerFactory.createServer()).thenReturn(jerseyServer);
        jerseyModule = new JerseyModule(jerseyServerFactory);
        jerseyModule.setContainer(container);
    }

    @Test
    public void testStart() throws Exception {

        jerseyModule.start(startedResult);

        verify(startedResult, times(0)).setResult(null);
        verify(startedResult, times(0)).setFailure(any(Throwable.class));

        verify(jerseyServer).init(any(JsonObject.class), any(Vertx.class), any(Container.class), handlerCaptor.capture());

        when(asyncResult.succeeded()).thenReturn(true).thenReturn(false);

        handlerCaptor.getValue().handle(asyncResult);
        verify(startedResult, times(1)).setResult(null);
        verify(startedResult, times(0)).setFailure(any(Throwable.class));

        handlerCaptor.getValue().handle(asyncResult);
        verify(startedResult, times(1)).setResult(null);
        verify(startedResult, times(1)).setFailure(any(Throwable.class));

    }

}
