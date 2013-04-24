package com.englishtown.vertx.jersey;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Container;

/**
 * The vert.x jersey http handler
 */
public interface JerseyHandler extends Handler<HttpServerRequest> {
    /**
     * Initialize the handler with vertx and container instances
     *
     * @param vertx     the vertx instance
     * @param container the container instance
     */
    void init(Vertx vertx, Container container);
}
