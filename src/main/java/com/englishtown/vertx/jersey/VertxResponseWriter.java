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
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 3/28/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enableResponseBuffering() {
        return false;
    }
}
