package com.englishtown.vertx.jersey.examples.guice.inject;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * Example guice implementation of {@link com.englishtown.vertx.jersey.inject.VertxRequestProcessor}
 */
public class GuiceRequestProcessor implements VertxRequestProcessor {
    /**
     * Provide additional async request processing
     *
     * @param vertxRequest  the vert.x http server request
     * @param jerseyRequest the jersey container request
     * @param done          the done async callback handler
     */
    @Override
    public void process(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done) {
        done.handle(null);
    }
}
