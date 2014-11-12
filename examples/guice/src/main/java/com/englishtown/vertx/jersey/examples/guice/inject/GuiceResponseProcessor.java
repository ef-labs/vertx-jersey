package com.englishtown.vertx.jersey.examples.guice.inject;

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import io.vertx.core.http.HttpServerResponse;
import org.glassfish.jersey.server.ContainerResponse;

/**
 * Example guice implementation of {@link com.englishtown.vertx.jersey.inject.VertxResponseProcessor}
 */
public class GuiceResponseProcessor implements VertxResponseProcessor {
    /**
     * <p>
     * Provide additional response processing
     * </p>
     * Note: This method is synchronous and must not block!
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
