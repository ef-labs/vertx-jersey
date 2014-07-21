package com.englishtown.vertx.jersey.examples.guice.inject;

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import org.glassfish.jersey.server.ContainerResponse;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * Created by adriangonzalez on 7/21/14.
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
