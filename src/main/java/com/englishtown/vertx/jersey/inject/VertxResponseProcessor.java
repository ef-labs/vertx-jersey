package com.englishtown.vertx.jersey.inject;

import org.glassfish.jersey.server.ContainerResponse;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * Injectable interface to provide additional vert.x response processing after Jersey.
 */
public interface VertxResponseProcessor {

    /**
     * <p>
     * Provide additional response processing
     * </p>
     * Note: This method is synchronous and must not block!
     *
     * @param vertxResponse  the vert.x http server response
     * @param jerseyResponse the jersey container response
     */
    public void handle(HttpServerResponse vertxResponse, ContainerResponse jerseyResponse);

}
