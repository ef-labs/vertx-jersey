package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import io.vertx.core.http.HttpServerResponse;
import org.glassfish.jersey.server.ContainerResponse;

public class OtherVertxResponseProcessor implements VertxResponseProcessor {
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
        vertxResponse.headers().add("x-other-response-processor", "ok");
    }
}
