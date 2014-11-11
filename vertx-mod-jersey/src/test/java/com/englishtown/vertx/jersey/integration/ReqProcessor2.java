package com.englishtown.vertx.jersey.integration;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;

import org.glassfish.jersey.server.ContainerRequest;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;

/**
 *
 */
public class ReqProcessor2 implements VertxRequestProcessor {

    private final Vertx vertx;

    @Inject
    public ReqProcessor2(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Provide additional async request processing
     *
     * @param vertxRequest  the vert.x http server request
     * @param jerseyRequest the jersey container request
     * @param done          the done async callback handler
     */
    @Override
    public void process(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, final Handler<Void> done) {
        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                done.handle(event);
            }
        });
    }
}
