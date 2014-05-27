package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import org.glassfish.jersey.server.ContainerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/11/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class OtherVertxRequestProcessor implements VertxRequestProcessor {

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
