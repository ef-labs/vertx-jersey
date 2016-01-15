package com.englishtown.vertx.jersey;

import io.vertx.core.Vertx;
import org.glassfish.jersey.server.spi.Container;

/**
 * Vert.x extension of {@link Container}
 */
public interface VertxContainer extends Container {

    /**
     * @param options
     * @deprecated Perform initialization at construction time
     */
    @Deprecated
    default void init(JerseyOptions options) {
        throw new UnsupportedOperationException();
    }

    /**
     * Starts the container
     */
    void start();

    /**
     * Stops the container
     */
    void stop();

    /**
     * Returns the current vertx instance
     *
     * @return the {@link Vertx} instance
     */
    Vertx getVertx();

    /**
     * Returns jersey configuration options
     *
     * @return
     */
    JerseyOptions getOptions();

    ApplicationHandlerDelegate getApplicationHandlerDelegate();

}
