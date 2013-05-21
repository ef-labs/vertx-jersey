package com.englishtown.vertx.jersey.inject;

import org.glassfish.jersey.server.ContainerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Injectable interface to provide additional async vert.x request processing before Jersey
 */
public interface VertxRequestProcessor {

    /**
     * Provide additional async request processing
     *
     * @param vertxRequest  the vert.x http server request
     * @param jerseyRequest the jersey container request
     * @param done          the done async callback handler
     */
    public void process(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done);

}
