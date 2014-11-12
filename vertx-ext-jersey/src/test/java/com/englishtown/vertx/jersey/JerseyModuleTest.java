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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * {@link JerseyModule} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class JerseyModuleTest {

    JerseyModule jerseyModule;

    @Mock
    JerseyServer jerseyServer;
    @Mock
    Provider<JerseyServer> jerseyServerProvider;
    @Mock
    JerseyConfigurator configurator;
    @Mock
    Provider<JerseyConfigurator> configuratorProvider;
    @Mock
    Future<Void> startedResult;
    @Mock
    AsyncResult<HttpServer> asyncResult;
    @Captor
    ArgumentCaptor<Handler<AsyncResult<HttpServer>>> handlerCaptor;

    @Before
    public void setUp() {
        when(jerseyServerProvider.get()).thenReturn(jerseyServer);
        when(configuratorProvider.get()).thenReturn(configurator);
        jerseyModule = new JerseyModule(jerseyServerProvider, configuratorProvider);
    }

    @Test
    public void testStart() throws Exception {

        jerseyModule.start(startedResult);

        verify(startedResult, times(0)).complete();
        verify(startedResult, times(0)).fail(any(Throwable.class));

        verify(jerseyServer).init(any(JerseyConfigurator.class), handlerCaptor.capture());

        when(asyncResult.succeeded()).thenReturn(true).thenReturn(false);

        handlerCaptor.getValue().handle(asyncResult);
        verify(startedResult, times(1)).complete();
        verify(startedResult, times(0)).fail(any(Throwable.class));

        handlerCaptor.getValue().handle(asyncResult);
        verify(startedResult, times(1)).complete();
        verify(startedResult, times(1)).fail(any(Throwable.class));

    }

}
