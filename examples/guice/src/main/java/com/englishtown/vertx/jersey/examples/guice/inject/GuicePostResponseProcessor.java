package com.englishtown.vertx.jersey.examples.guice.inject;

import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import org.glassfish.jersey.server.ContainerResponse;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * Example guice implementation of {@link com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor}
 */
public class GuicePostResponseProcessor implements VertxPostResponseProcessor {
    /**
     * <p>
     * Provide processing after the the vert.x response end() method has been called
     * </p>
     *
     * @param vertxResponse  the vert.x http server response
     * @param jerseyResponse the jersey container response
     */
    @Override
    public void process(HttpServerResponse vertxResponse, ContainerResponse jerseyResponse) {
        if (jerseyResponse != null) {

        }
    }
}
