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

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.VertxContainer;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link DefaultJerseyHandler} unit tests
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJerseyHandlerTest {

    DefaultJerseyHandler jerseyHandler;
    JsonObject config = new JsonObject();
    List<VertxRequestProcessor> requestProcessors = new ArrayList<>();

    @Mock
    VertxContainer container;
    @Mock
    JerseyOptions options;
    @Mock
    ApplicationHandlerDelegate applicationHandlerDelegate;
    @Mock
    HttpServerRequest request;
    @Mock
    HttpServerResponse response;
    @Mock
    ServiceLocator serviceLocator;
    @Mock
    Vertx vertx;
    @Mock
    Logger logger;
    @Mock
    Ref<Vertx> vertxRef;
    @Mock
    Ref<Container> containerRef;
    @Mock
    MultiMap headers;
    @Captor
    ArgumentCaptor<Handler<Buffer>> dataHandlerCaptor;
    @Captor
    ArgumentCaptor<Handler<Void>> endHandlerCaptor;

    @Before
    public void setUp() {

        when(container.getOptions()).thenReturn(options);
        when(container.getVertx()).thenReturn(vertx);
        when(container.getApplicationHandlerDelegate()).thenReturn(applicationHandlerDelegate);
        when(applicationHandlerDelegate.getServiceLocator()).thenReturn(serviceLocator);
        when(options.getMaxBodySize()).thenReturn(1024);
        when(options.getBaseUri()).thenReturn(URI.create("/test"));

        when(request.absoluteURI()).thenReturn(URI.create("http://test.englishtown.com/test").toString());
        when(request.response()).thenReturn(response);
        when(request.headers()).thenReturn(headers);

        JsonArray resources = new JsonArray().add("com.englishtown.vertx.jersey.resources");
        config.put(DefaultJerseyOptions.CONFIG_RESOURCES, resources);

        ContainerResponseWriterProvider provider = mock(ContainerResponseWriterProvider.class);
        when(provider.get(any(HttpServerRequest.class), any(ContainerRequest.class))).thenReturn(mock(ContainerResponseWriter.class));

        when(serviceLocator.<Ref<Vertx>>getService((new TypeLiteral<Ref<Vertx>>() {
        }).getType())).thenReturn(vertxRef);
        when(serviceLocator.<Ref<Container>>getService((new TypeLiteral<Ref<Container>>() {
        }).getType())).thenReturn(containerRef);

        jerseyHandler = new DefaultJerseyHandler(() -> container, provider, requestProcessors);
    }

    @Test
    public void testHandle() throws Exception {

        when(request.method()).thenReturn(HttpMethod.GET);
        when(request.headers()).thenReturn(mock(MultiMap.class));

        jerseyHandler.handle(request);
        verify(applicationHandlerDelegate).handle(any(ContainerRequest.class));

    }

    @Test
    public void testHandle_JSON_POST() throws Exception {

        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        when(request.method()).thenReturn(HttpMethod.POST);
        when(request.headers()).thenReturn(new HeadersAdaptor(headers));

        jerseyHandler.handle(request);

        verify(request).handler(dataHandlerCaptor.capture());
        verify(request).endHandler(endHandlerCaptor.capture());

        Buffer data = Buffer.buffer("{}");
        dataHandlerCaptor.getValue().handle(data);
        endHandlerCaptor.getValue().handle(null);

        verify(applicationHandlerDelegate).handle(any(ContainerRequest.class));

    }

    @Test
    public void testHandle_RequestProcessors() throws Exception {

        when(request.headers()).thenReturn(mock(MultiMap.class));
        when(request.method()).thenReturn(HttpMethod.GET);
        InputStream inputStream = null;

        VertxRequestProcessor rp1 = mock(VertxRequestProcessor.class);
        VertxRequestProcessor rp2 = mock(VertxRequestProcessor.class);

        requestProcessors.add(rp1);
        requestProcessors.add(rp2);

        jerseyHandler.handle(request, inputStream);

        verify(rp1).process(any(HttpServerRequest.class), any(ContainerRequest.class), endHandlerCaptor.capture());
        endHandlerCaptor.getValue().handle(null);
        verify(rp2).process(any(HttpServerRequest.class), any(ContainerRequest.class), endHandlerCaptor.capture());
        endHandlerCaptor.getValue().handle(null);

        verify(applicationHandlerDelegate).handle(any(ContainerRequest.class));
    }

    @Test
    public void testHandle_RequestProcessors_Throw() throws Exception {

        when(request.headers()).thenReturn(mock(MultiMap.class));
        when(request.method()).thenReturn(HttpMethod.GET);
        InputStream inputStream = null;

        VertxRequestProcessor rp1 = mock(VertxRequestProcessor.class);
        VertxRequestProcessor rp2 = mock(VertxRequestProcessor.class);

        when(response.setStatusCode(anyInt())).thenReturn(response);
        doThrow(RuntimeException.class).when(rp2).process(any(), any(), any());

        requestProcessors.add(rp1);
        requestProcessors.add(rp2);

        jerseyHandler.handle(request, inputStream);

        verify(rp1).process(any(), any(), endHandlerCaptor.capture());
        endHandlerCaptor.getValue().handle(null);

        verify(rp2).process(any(), any(), any());
        verify(response).setStatusCode(eq(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
        verify(response).end();
    }

    @Test
    public void testShouldReadData() throws Exception {

        boolean result;

        DefaultHttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);

        when(request.method()).thenReturn(HttpMethod.GET).thenReturn(HttpMethod.PUT);
        when(request.headers()).thenReturn(new HeadersAdaptor(headers));

        result = jerseyHandler.shouldReadData(request);
        assertFalse(result);

        result = jerseyHandler.shouldReadData(request);
        assertFalse(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        when(request.method()).thenReturn(HttpMethod.POST);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + "; charset=UTF-8");

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        when(request.method()).thenReturn(HttpMethod.PATCH);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

        headers.clear();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED + "; charset=UTF-8");

        result = jerseyHandler.shouldReadData(request);
        assertTrue(result);

    }

    @Test
    public void testGetAbsoluteURI() throws Exception {
        URI uri;

        String absoluteUri = "http://0.0.0.0:80/test";
        String host = "test.englishtown.com";
        String badUrl = "http://test.englishtown.com/test?a=b=c|d=e";

        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.absoluteURI()).thenReturn(absoluteUri).thenThrow(new IllegalArgumentException());
        when(request.uri()).thenReturn(badUrl);
        when(request.headers()).thenReturn(headers);
        when(headers.get(eq(HttpHeaders.HOST))).thenReturn(host);

        uri = jerseyHandler.getAbsoluteURI(request);
        assertEquals("http://test.englishtown.com/test", uri.toString());

        uri = jerseyHandler.getAbsoluteURI(request);
        assertEquals("http://test.englishtown.com/test?a=b%3Dc%7Cd%3De", uri.toString());

    }

    @Test
    public void testGetBaseURI() throws Exception {
        URI baseUri = URI.create("/test");
        when(options.getBaseUri()).thenReturn(baseUri);

        URI result = jerseyHandler.getBaseUri();
        assertEquals(baseUri, result);
    }

}
