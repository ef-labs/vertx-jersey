package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.WriteStreamOutput;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * Default implementation of {@link com.englishtown.vertx.jersey.WriteStreamOutput}
 */
public class DefaultWriteStreamOutput implements WriteStreamOutput {

    private HttpServerResponse response;
    private Handler<Void> endHandler;

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteStreamOutput init(HttpServerResponse response, Handler<Void> endHandler) {
        this.response = response;
        this.endHandler = endHandler;
        return this;
    }

    /**
     * Flag to indicate if the {@link org.vertx.java.core.http.HttpServerResponse} has been set yet.
     *
     * @return
     */
    @Override
    public boolean isResponseSet() {
        return (response != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end() {
        checkResponseSet();
        if (endHandler != null) {
            endHandler.handle(null);
        } else {
            response.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteStreamOutput write(Buffer data) {
        checkResponseSet();
        response.write(data);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteStreamOutput setWriteQueueMaxSize(int maxSize) {
        checkResponseSet();
        response.setWriteQueueMaxSize(maxSize);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean writeQueueFull() {
        checkResponseSet();
        return response.writeQueueFull();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteStreamOutput drainHandler(Handler<Void> handler) {
        checkResponseSet();
        response.drainHandler(handler);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WriteStreamOutput exceptionHandler(Handler<Throwable> handler) {
        checkResponseSet();
        response.exceptionHandler(handler);
        return this;
    }

    private void checkResponseSet() {
        if (response == null) throw new IllegalStateException("The HttpServerResponse has not been set yet.");
    }

}
