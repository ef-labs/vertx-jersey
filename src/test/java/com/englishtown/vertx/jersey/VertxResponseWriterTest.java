package com.englishtown.vertx.jersey;

import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/22/13
 * Time: 11:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class VertxResponseWriterTest {

    @Test
    public void testWriteResponseStatusAndHeaders() throws Exception {

        HttpServerResponse response = mock(HttpServerResponse.class);
        VertxResponseWriter writer = createInstance(response, mock(Vertx.class));

        ContainerResponse cr = mock(ContainerResponse.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(cr.getStatusInfo()).thenReturn(mock(Response.StatusType.class));
        when(cr.getHeaders()).thenReturn(headers);

        headers.add("x-test", "custom header");
        OutputStream outputStream = writer.writeResponseStatusAndHeaders(15, cr);

        assertNotNull(outputStream);
        verify(response, times(1)).setStatusCode(anyInt());
        verify(response, times(1)).setStatusMessage(anyString());
        verify(response, times(2)).putHeader(anyString(), anyObject());

        writer.writeResponseStatusAndHeaders(-1, cr);
        verify(response, times(3)).putHeader(anyString(), anyObject());

    }

    @Test
    public void testWrite() throws Exception {

        HttpServerResponse response = mock(HttpServerResponse.class);
        VertxResponseWriter writer = createInstance(response, mock(Vertx.class));

        ContainerResponse cr = mock(ContainerResponse.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(cr.getStatusInfo()).thenReturn(mock(Response.StatusType.class));
        when(cr.getHeaders()).thenReturn(headers);

        Map<String, Object> vertxHeaders = new HashMap<>();
        vertxHeaders.put(HttpHeaders.CONTENT_LENGTH, 12);
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

        HttpServerResponse response = mock(HttpServerResponse.class);
        VertxResponseWriter writer = createInstance(response, mock(Vertx.class));

        ContainerResponse cr = mock(ContainerResponse.class);
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(cr.getStatusInfo()).thenReturn(mock(Response.StatusType.class));
        when(cr.getHeaders()).thenReturn(headers);
        when(cr.isChunked()).thenReturn(true);

        OutputStream outputStream = writer.writeResponseStatusAndHeaders(-1, cr);
        verify(response, times(1)).setChunked(eq(true));

        outputStream.write("Chunked data".getBytes());
        outputStream.flush();
        verify(response, times(1)).write(any(Buffer.class));

        outputStream.flush();
        verify(response, times(1)).write(any(Buffer.class));

        outputStream.write("Chunked data".getBytes());
        outputStream.flush();
        verify(response, times(2)).write(any(Buffer.class));

        outputStream.write("Chunked data".getBytes());
        outputStream.flush();
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

    private VertxResponseWriter createInstance() {
        return createInstance(mock(HttpServerResponse.class), mock(Vertx.class));
    }

    private VertxResponseWriter createInstance(HttpServerResponse responseMock, Vertx vertxMock) {

        HttpServerRequest request = mock(HttpServerRequest.class);
        when(request.response()).thenReturn(responseMock);

        long id = 10;
        when(vertxMock.setTimer(anyLong(), any(Handler.class))).thenReturn(id);

        return new VertxResponseWriter(request, vertxMock, mock(Logger.class));

    }

    @Test
    public void testSuspend() throws Exception {

        Vertx vertx = mock(Vertx.class);
        VertxResponseWriter writer = createInstance(mock(HttpServerResponse.class), vertx);
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

        Vertx vertx = mock(Vertx.class);
        VertxResponseWriter writer = createInstance(mock(HttpServerResponse.class), vertx);
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

        HttpServerResponse response = mock(HttpServerResponse.class);
        VertxResponseWriter writer = createInstance(response, mock(Vertx.class));

        writer.commit();
        verify(response, times(1)).end();

    }

    @Test
    public void testFailure() throws Exception {

        HttpServerResponse response = mock(HttpServerResponse.class);
        VertxResponseWriter writer = createInstance(response, mock(Vertx.class));
        Throwable error = mock(Throwable.class);

        writer.failure(error);
        verify(response, times(1)).setStatusCode(eq(500));
        verify(response, times(1)).setStatusMessage(eq("Internal Server Error"));
        verify(response, times(1)).end();

    }

    @Test
    public void testEnableResponseBuffering() throws Exception {

        VertxResponseWriter writer = createInstance();
        boolean result;

        result = writer.enableResponseBuffering();
        assertFalse(result);

    }
}
