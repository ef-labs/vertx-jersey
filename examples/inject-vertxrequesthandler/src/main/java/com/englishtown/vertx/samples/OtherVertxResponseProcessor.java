package com.englishtown.vertx.samples;

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import org.glassfish.jersey.server.ContainerResponse;
import org.vertx.java.core.http.HttpServerResponse;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/24/13
 * Time: 11:15 AM
 * To change this template use File | Settings | File Templates.
 */
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
