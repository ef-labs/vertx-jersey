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

        private final HttpServerResponse response;
        private Buffer buffer = new Buffer();

        private VertxOutputStream(HttpServerResponse response) {
            this.response = response;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) throws IOException {
            buffer.appendInt(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
            buffer.appendBytes(b);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
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
            if (buffer != null && buffer.length() > 0) {
                if (!response.headers().containsKey(HttpHeaders.CONTENT_LENGTH)) {
                    response.headers().put(HttpHeaders.CONTENT_LENGTH, buffer.length());
                }
                response.write(buffer);
                buffer = null;
            }
        }

    }

    private static class VertxChunkedOutputStream extends OutputStream {

        private final HttpServerResponse response;

        private VertxChunkedOutputStream(HttpServerResponse response) {
            this.response = response;
            response.setChunked(true);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(int b) throws IOException {
            Buffer buffer = new Buffer(1).appendInt(b);
            response.write(buffer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b) throws IOException {
            Buffer buffer = new Buffer(b);
            response.write(buffer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            Buffer buffer;

            if (off == 0 && len == b.length) {
                buffer = new Buffer(b);
            } else {
                buffer = new Buffer(Arrays.copyOfRange(b, off, off + len));
            }

            response.write(buffer);
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
        HttpServerResponse response = vertxRequest.response;

        // Write the status
        response.statusCode = responseContext.getStatus();
        response.statusMessage = responseContext.getStatusInfo().getReasonPhrase();

        // Set the content length header
        if (contentLength != -1) {
            response.putHeader(HttpHeaders.CONTENT_LENGTH, contentLength);
        }

        // Set chunked if response entity is ChunkedOutput<T>
        outputStream = responseContext.isChunked() ? new VertxChunkedOutputStream(response)
                : new VertxOutputStream(response);

        // TODO: Set keep alive?
        //vertxRequest.response.putHeader("Connection", "keep-alive");

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
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        vertxRequest.response.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failure(Throwable error) {

        logger.error(error.getMessage(), error);
        HttpServerResponse response = vertxRequest.response;

        // Set error status and end
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        response.statusCode = status.getStatusCode();
        response.statusMessage = status.getReasonPhrase();
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
