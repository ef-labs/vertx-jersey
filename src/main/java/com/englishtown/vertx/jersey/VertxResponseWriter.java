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

import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;

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
class VertxResponseWriter implements ContainerResponseWriter {

    private static class VertxOutputStream extends OutputStream {

        protected final HttpServerResponse response;
        protected Buffer buffer = new Buffer();
        protected boolean isClosed;

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
            if (buffer.length() > 0 && response.headers().containsKey(HttpHeaders.CONTENT_LENGTH)) {
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
                if (!response.headers().containsKey(HttpHeaders.CONTENT_LENGTH)) {
                    response.headers().put(HttpHeaders.CONTENT_LENGTH, buffer.length());
                }
                response.write(buffer);
            }
            buffer = null;
            isClosed = true;
        }

        protected void checkState() {
            if (isClosed) {
                throw new RuntimeException("Stream closed");
            }
        }
    }

    private static class VertxChunkedOutputStream extends VertxOutputStream {

        private VertxChunkedOutputStream(HttpServerResponse response) {
            super(response);
            response.setChunked(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() throws IOException {
            checkState();
            if (buffer.length() > 0) {
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
            if (buffer != null && buffer.length() > 0) {
                response.write(buffer);
            }
            buffer = null;
            isClosed = true;
        }
    }

    private final HttpServerRequest vertxRequest;
    private final Logger logger;
    private OutputStream outputStream;

    public VertxResponseWriter(HttpServerRequest vertxRequest, Logger logger) {
        this.vertxRequest = vertxRequest;
        this.logger = logger;
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
            response.putHeader(HttpHeaders.CONTENT_LENGTH, contentLength);
        }

        // Set chunked if response entity is ChunkedOutput<T>
        outputStream = responseContext.isChunked() ? new VertxChunkedOutputStream(response)
                : new VertxOutputStream(response);

        for (final Map.Entry<String, List<Object>> e : responseContext.getHeaders().entrySet()) {
            for (final Object value : e.getValue()) {
                response.putHeader(e.getKey(), value);
            }
        }

        return this.outputStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean suspend(long timeOut, TimeUnit timeUnit, TimeoutHandler timeoutHandler) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSuspendTimeout(long timeOut, TimeUnit timeUnit) throws IllegalStateException {
        // TODO: setSuspendTimeout
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

        logger.error(error.getMessage(), error);
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
