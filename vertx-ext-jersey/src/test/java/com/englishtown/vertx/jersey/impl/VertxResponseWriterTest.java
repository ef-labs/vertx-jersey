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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.HeadersAdaptor;
import io.vertx.core.logging.Logger;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;

/**
 * {@link VertxResponseWriter} unit tests
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class VertxResponseWriterTest {

    VertxResponseWriter writer;
    List<VertxResponseProcessor> responseProcessors = new ArrayList<>();
    List<VertxPostResponseProcessor> postResponseProcessors = new ArrayList<>();
    long timerId = 10;

    @Mock
    Vertx vertx;
    @Mock
    Logger logger;
    @Mock
    HttpServerRequest request;
    @Mock
    HttpServerResponse response;

    @Before
    public void setUp() {
        when(request.response()).thenReturn(response);
        when(vertx.setTimer(anyLong(), any(Handler.class))).thenReturn(timerId);

        writer = new VertxResponseWriter(request, vertx, responseProcessors, postResponseProcessors);
    }

    @Test
    public void testWriteResponseStatusAndHeaders() throws Exception {

        ContainerResponse cr = mock(ContainerResponse.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        when(cr.getStatusInfo()).thenReturn(mock(Response.StatusType.class));
        when(cr.getStringHeaders()).thenReturn(headers);

        VertxResponseProcessor processor1 = mock(VertxResponseProcessor.class);
        VertxResponseProcessor processor2 = mock(VertxResponseProcessor.class);
        responseProcessors.add(processor1);
        responseProcessors.add(processor2);

        headers.add("x-test", "custom header");
        OutputStream outputStream = writer.writeResponseStatusAndHeaders(15, cr);

        assertNotNull(outputStream);
        verify(response, times(1)).setStatusCode(anyInt());
        verify(response, times(1)).setStatusMessage(anyString());
        verify(response, times(2)).putHeader(anyString(), anyString());
        verify(processor1).process(eq(response), eq(cr));
        verify(processor2).process(eq(response), eq(cr));

        writer.writeResponseStatusAndHeaders(-1, cr);
        verify(response, times(3)).putHeader(anyString(), anyString());

    }

    @Test
    public void testWrite() throws Exception {

        ContainerResponse cr = mock(ContainerResponse.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        when(cr.getStatusInfo()).thenReturn(mock(Response.StatusType.class));
        when(cr.getStringHeaders()).thenReturn(headers);

        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        MultiMap vertxHeaders = new HeadersAdaptor(httpHeaders);
        vertxHeaders.add(HttpHeaders.CONTENT_LENGTH, "12");
        when(response.headers()).thenReturn(vertxHeaders);

        OutputStream outputStream = writer.writeResponseStatusAndHeaders(12, cr);

        outputStream.write("callback".getBytes());
        outputStream.write(40);
        outputStream.flush();
        verify(response, times(1)).write(any(Buffer.class));

        byte[] buffer = new byte[1024];
        buffer[0] = "{".getBytes()[0];
        buffer[1] = "}".getBytes()[0];
        outputStream.write(buffer, 0, 2);
        outputStream.write(41);
        outputStream.flush();
        verify(response, times(2)).write(any(Buffer.class));

        outputStream.write(";".getBytes());
        outputStream.close();
        verify(response, times(3)).write(any(Buffer.class));

        try {
            outputStream.write("fail".getBytes());
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

    }

    @Test
    public void testWrite_Chunked() throws Exception {

        ContainerResponse cr = mock(ContainerResponse.class);
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        when(cr.getStatusInfo()).thenReturn(mock(Response.StatusType.class));
        when(cr.getStringHeaders()).thenReturn(headers);
        when(cr.isChunked()).thenReturn(true);

        OutputStream outputStream = writer.writeResponseStatusAndHeaders(-1, cr);
        verify(response, times(1)).setChunked(eq(true));

        outputStream.write("Chunked data".getBytes());
        verify(response, times(1)).write(any(Buffer.class));

        outputStream.flush();
        verify(response, times(1)).write(any(Buffer.class));

        outputStream.write("Chunked data".getBytes());
        verify(response, times(2)).write(any(Buffer.class));

        outputStream.write("Chunked data".getBytes());
        verify(response, times(3)).write(any(Buffer.class));

        outputStream.write("Final chunked data".getBytes());
        outputStream.close();
        verify(response, times(4)).write(any(Buffer.class));

        try {
            outputStream.write("fail".getBytes());
            fail();
        } catch (RuntimeException e) {
            // Expected
        }

    }

    @Test
    public void testSuspend() throws Exception {

        boolean result;
        ContainerResponseWriter.TimeoutHandler timeoutHandler = mock(ContainerResponseWriter.TimeoutHandler.class);

        result = writer.suspend(10, TimeUnit.SECONDS, timeoutHandler);
        assertTrue(result);
        verify(vertx, times(1)).setTimer(anyLong(), any(Handler.class));

        result = writer.suspend(0, TimeUnit.SECONDS, timeoutHandler);
        assertTrue(result);
        verify(vertx, times(1)).setTimer(anyLong(), any(Handler.class));

        result = writer.suspend(10, TimeUnit.SECONDS, timeoutHandler);
        assertTrue(result);
        verify(vertx, times(2)).setTimer(anyLong(), any(Handler.class));

    }

    @Test
    public void testSetSuspendTimeout() throws Exception {

        ContainerResponseWriter.TimeoutHandler timeoutHandler = mock(ContainerResponseWriter.TimeoutHandler.class);

        try {
            writer.setSuspendTimeout(10, TimeUnit.SECONDS);
            fail();
        } catch (IllegalStateException e) {
            // Expected
        }

        writer.suspend(0, TimeUnit.SECONDS, timeoutHandler);
        writer.setSuspendTimeout(5, TimeUnit.MINUTES);
        verify(vertx, times(1)).setTimer(anyLong(), any(Handler.class));

    }

    @Test
    public void testCommit() throws Exception {

        VertxPostResponseProcessor processor1 = mock(VertxPostResponseProcessor.class);
        VertxPostResponseProcessor processor2 = mock(VertxPostResponseProcessor.class);
        postResponseProcessors.add(processor1);
        postResponseProcessors.add(processor2);

        writer.commit();
        verify(response).end();
        verify(processor1).process(eq(response), any(ContainerResponse.class));
        verify(processor2).process(eq(response), any(ContainerResponse.class));

    }

    @Test
    public void testFailure() throws Exception {

        Throwable error = mock(Throwable.class);

        writer.failure(error);
        verify(response, times(1)).setStatusCode(eq(500));
        verify(response, times(1)).setStatusMessage(eq("Internal Server Error"));
        verify(response, times(1)).end();

    }

    @Test
    public void testEnableResponseBuffering() throws Exception {

        boolean result;

        result = writer.enableResponseBuffering();
        assertFalse(result);

    }
}
