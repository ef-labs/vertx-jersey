package com.englishtown.vertx.jersey;

import io.vertx.core.http.HttpServerOptions;

/**
 * Vert.x http server configuration
 */
public interface JerseyServerOptions {

    /**
     * Gets the {@link HttpServerOptions} used to set up the vert.x http server
     *
     * @return
     */
    HttpServerOptions getServerOptions();

}
