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

package com.englishtown.vertx.jersey.inject.impl;

import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * {@link VertxResponseWriterProvider} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class VertxResponseWriterProviderTest {

    VertxResponseWriterProvider provider;
    List<VertxResponseProcessor> responseProcessors = new ArrayList<>();
    List<VertxPostResponseProcessor> postResponseProcessors = new ArrayList<>();

    @Mock
    Vertx vertx;
    @Mock
    Container container;
    @Mock
    HttpServerRequest vertxRequest;
    @Mock
    ContainerRequest jerseyRequest;
    @Mock
    VertxResponseProcessor responseProcessor;
    @Mock
    VertxPostResponseProcessor postResponseProcessor;

    @Before
    public void setUp() {

        responseProcessors.add(responseProcessor);
        postResponseProcessors.add(postResponseProcessor);

        provider = new VertxResponseWriterProvider(vertx, responseProcessors, postResponseProcessors);

    }

    @Test
    public void testGet() throws Exception {

        ContainerResponseWriter writer;

        writer = provider.get(vertxRequest, jerseyRequest);
        assertNotNull(writer);
    }
}
