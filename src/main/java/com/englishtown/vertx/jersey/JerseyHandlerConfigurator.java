package com.englishtown.vertx.jersey;

import org.glassfish.jersey.server.ApplicationHandler;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import java.net.URI;

/**
 * Provides configuration for a {@link JerseyHandler}
 */
public interface JerseyHandlerConfigurator {

    void init(Vertx vertx, Container container);

    /**
     * Returns the base URI used by Jersey
     *
     * @return base URI
     */
    URI getBaseUri();

    /**
     * Returns the Jersey {@link ApplicationHandler} instance
     *
     * @return the application handler instance
     */
    ApplicationHandlerDelegate getApplicationHandler();

    /**
     * The max body size in bytes when reading the vert.x input stream
     *
     * @return the max body size bytes
     */
    int getMaxBodySize();

}
