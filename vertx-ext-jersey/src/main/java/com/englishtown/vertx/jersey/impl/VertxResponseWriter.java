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

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferFactoryImpl;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.core.spi.BufferFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;

import com.englishtown.vertx.jersey.WriteStreamOutput;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;

/**
 * A Jersey ContainerResponseWriter to write to the vertx response
 */
public class VertxResponseWriter implements ContainerResponseWriter {

    private static final Logger log = LoggerFactory.getLogger(VertxResponseWriter.class);

    private static class VertxOutputStream extends OutputStream {

        final HttpServerResponse response;
        Buffer buffer = new BufferFactoryImpl().buffer();
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
                buffer = new BufferFactoryImpl().buffer();
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
        private final BufferFactory bufferFactory = new BufferFactoryImpl();

        private VertxChunkedOutputStream(HttpServerResponse response) {
            this.response = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) throws IOException {
            checkState();
            Buffer buffer = bufferFactory.buffer();
            buffer.appendByte((byte) b);
            response.write(buffer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
            checkState();
            response.write(bufferFactory.buffer(b));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkState();
            Buffer buffer = bufferFactory.buffer();
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

    private static class NOPOutputStream extends OutputStream {

        private NOPOutputStream() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) throws IOException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

    }

    private final HttpServerRequest vertxRequest;
    private final Vertx vertx;
    private final List<VertxResponseProcessor> responseProcessors;
    private final List<VertxPostResponseProcessor> postResponseProcessors;

    private long suspendTimerId;
    private TimeoutHandler timeoutHandler;
    private ContainerResponse jerseyResponse;
    private boolean isWriteStream;

    @Inject
    public VertxResponseWriter(
            HttpServerRequest vertxRequest,
            Vertx vertx,
            List<VertxResponseProcessor> responseProcessors,
            List<VertxPostResponseProcessor> postResponseProcessors) {
        this.vertxRequest = vertxRequest;
        this.vertx = vertx;
        this.responseProcessors = responseProcessors;
        this.postResponseProcessors = postResponseProcessors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) throws ContainerException {
        jerseyResponse = responseContext;
        HttpServerResponse response = vertxRequest.response();

        // Write the status
        response.setStatusCode(responseContext.getStatus());
        response.setStatusMessage(responseContext.getStatusInfo().getReasonPhrase());

        // Set the content length header
        if (contentLength != -1) {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength));
        }

        for (final Map.Entry<String, List<String>> e : responseContext.getStringHeaders().entrySet()) {
            for (final String value : e.getValue()) {
                response.putHeader(e.getKey(), value);
            }
        }

        // Run any response processors
        if (!responseProcessors.isEmpty()) {
            for (VertxResponseProcessor processor : responseProcessors) {
                processor.process(response, responseContext);
            }
        }

        // Return output stream based on whether entity is chunked
        if (responseContext.isChunked()) {
            response.setChunked(true);
            return new VertxChunkedOutputStream(response);
        } else if (responseContext.hasEntity() && WriteStreamOutput.class.isAssignableFrom(responseContext.getEntityClass())) {
            WriteStreamOutput writeStreamOutput = (WriteStreamOutput) responseContext.getEntity();
            writeStreamOutput.init(response, event -> {
                end();
            });
            isWriteStream = true;
            return new NOPOutputStream();
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
        this.suspendTimerId = vertx.setTimer(ms, id -> {
            if (id == suspendTimerId) {
                VertxResponseWriter.this.timeoutHandler.onTimeout(VertxResponseWriter.this);
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
        // End the vertx response if not a write stream
        if (!isWriteStream) {
            end();
        }
    }

    protected void end() {
        // End the vertx response
        vertxRequest.response().end();

        // Call any post response processors
        if (!postResponseProcessors.isEmpty()) {
            for (VertxPostResponseProcessor processor : postResponseProcessors) {
                processor.process(vertxRequest.response(), jerseyResponse);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failure(Throwable error) {

        log.error(error.getMessage(), error);
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
