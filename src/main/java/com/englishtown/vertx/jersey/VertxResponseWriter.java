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

import com.hazelcast.nio.FastByteArrayOutputStream;
import org.glassfish.jersey.server.ContainerException;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A Jersey ContainerResponseWriter to write to the vertx response
 */
class VertxResponseWriter implements ContainerResponseWriter {

    private final HttpServerRequest vertxRequest;
    private final FastByteArrayOutputStream outputStream;

    public VertxResponseWriter(HttpServerRequest vertxRequest) {
        this.vertxRequest = vertxRequest;
        this.outputStream = new FastByteArrayOutputStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse responseContext) throws ContainerException {

        // Write the status
        vertxRequest.response.statusCode = responseContext.getStatus();
        vertxRequest.response.statusMessage = responseContext.getStatusInfo().getReasonPhrase();

        // Set the content length header
        if (contentLength != -1 && contentLength < Integer.MAX_VALUE) {
            vertxRequest.response.putHeader("Content-Length", contentLength);
        }

        // TODO: Set keep alive?
        //vertxRequest.response.putHeader("Connection", "keep-alive");

        for (final Map.Entry<String, List<Object>> e : responseContext.getHeaders().entrySet()) {
            for (final Object value : e.getValue()) {
                vertxRequest.response.putHeader(e.getKey(), value);
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
        if (this.outputStream.size() > 0) {
            vertxRequest.response.end(new Buffer(this.outputStream.toByteArray()));
        } else {
            vertxRequest.response.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failure(Throwable error) {
        // TODO: Add error handling
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enableResponseBuffering() {
        // TODO enableResponseBuffering?
        return false;
    }
}
