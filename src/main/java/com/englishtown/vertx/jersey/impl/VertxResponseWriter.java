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

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A Jersey ContainerResponseWriter to write to the vertx response
 */
public class VertxResponseWriter implements ContainerResponseWriter {

    private static class VertxOutputStream extends OutputStream {

        final HttpServerResponse response;
        Buffer buffer = new Buffer();
        boolean isClosed;

        private VertxOutputStream(HttpServerResponse response) {
            this.response = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) throws IOException {
            checkState();
            buffer.appendByte((byte) b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
            checkState();
            buffer.appendBytes(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkState();
            if (off == 0 && len == b.length) {
                buffer.appendBytes(b);
            } else {
                buffer.appendBytes(Arrays.copyOfRange(b, off, off + len));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() throws IOException {
            checkState();
            // Only flush to underlying very.x response if the content-length has been set
            if (buffer.length() > 0 && response.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
                response.write(buffer);
                buffer = new Buffer();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            // Write any remaining buffer to the vert.x response
            // Set content-length if not set yet
            if (buffer != null && buffer.length() > 0) {
                if (!response.headers().contains(HttpHeaders.CONTENT_LENGTH)) {
                    response.headers().add(HttpHeaders.CONTENT_LENGTH, String.valueOf(buffer.length()));
                }
                response.write(buffer);
            }
            buffer = null;
            isClosed = true;
        }

        void checkState() {
            if (isClosed) {
                throw new RuntimeException("Stream is closed");
            }
        }
    }

    private static class VertxChunkedOutputStream extends OutputStream {

        private final HttpServerResponse response;
        private boolean isClosed;

        private VertxChunkedOutputStream(HttpServerResponse response) {
            this.response = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) throws IOException {
            checkState();
            Buffer buffer = new Buffer();
            buffer.appendByte((byte) b);
            response.write(buffer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
            checkState();
            response.write(new Buffer(b));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkState();
            Buffer buffer = new Buffer();
            if (off == 0 && len == b.length) {
                buffer.appendBytes(b);
            } else {
                buffer.appendBytes(Arrays.copyOfRange(b, off, off + len));
            }
            response.write(buffer);
        }

        @Override
        public void close() throws IOException {
            isClosed = true;
        }

        void checkState() {
            if (isClosed) {
                throw new RuntimeException("Stream is closed");
            }
        }

    }

    private final HttpServerRequest vertxRequest;
    private final Vertx vertx;
    private final Container container;
    private final List<VertxResponseProcessor> responseProcessors;

    private long suspendTimerId;
    private TimeoutHandler timeoutHandler;

    @Inject
    public VertxResponseWriter(
            HttpServerRequest vertxRequest,
            Vertx vertx,
            Container container,
            List<VertxResponseProcessor> responseProcessors) {
        this.vertxRequest = vertxRequest;
        this.vertx = vertx;
        this.container = container;
        this.responseProcessors = responseProcessors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) throws ContainerException {
        HttpServerResponse response = vertxRequest.response();

        // Write the status
        response.setStatusCode(responseContext.getStatus());
        response.setStatusMessage(responseContext.getStatusInfo().getReasonPhrase());

        // Set the content length header
        if (contentLength != -1) {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        }

        for (final Map.Entry<String, List<Object>> e : responseContext.getHeaders().entrySet()) {
            for (final Object value : e.getValue()) {
                response.putHeader(e.getKey(), String.valueOf(value));
            }
        }

        // Run any response processors
        for (VertxResponseProcessor processor : responseProcessors) {
            processor.process(response, responseContext);
        }

        // Return output stream based on whether entity is chunked
        if (responseContext.isChunked()) {
            response.setChunked(true);
            return new VertxChunkedOutputStream(response);
        } else {
            return new VertxOutputStream(response);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
        // TODO: If already suspended should return false according to documentation
        // Store the timeout handler
        this.timeoutHandler = timeoutHandler;

        // Cancel any existing timer
        if (suspendTimerId != 0) {
            vertx.cancelTimer(suspendTimerId);
            suspendTimerId = 0;
        }

        // If timeout <= 0, then it suspends indefinitely
        if (timeOut <= 0) {
            return true;
        }

        // Get milliseconds
        long ms = timeUnit.toMillis(timeOut);

        // Schedule timeout on the event loop
        this.suspendTimerId = vertx.setTimer(ms, new Handler<Long>() {
            @Override
            public void handle(Long id) {
                if (id == suspendTimerId) {
                    VertxResponseWriter.this.timeoutHandler.onTimeout(VertxResponseWriter.this);
                }
            }
        });

        return true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {

        if (timeoutHandler == null) {
            throw new IllegalStateException("The timeoutHandler is null");
        }

        suspend(timeOut, timeUnit, timeoutHandler);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        vertxRequest.response().end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failure(Throwable error) {

        container.logger().error(error.getMessage(), error);
        HttpServerResponse response = vertxRequest.response();

        // Set error status and end
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        response.setStatusCode(status.getStatusCode());
        response.setStatusMessage(status.getReasonPhrase());
        response.end();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enableResponseBuffering() {
        return false;
    }
}
