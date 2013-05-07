package com.englishtown.vertx.jersey;

import org.glassfish.jersey.server.ContainerRequest;

/**
 * Delegates handling a Jersey {@link ContainerRequest} to an underlying
 * {@link org.glassfish.jersey.server.ApplicationHandler}
 */
public interface ApplicationHandlerDelegate {

    /**
     * The main request/response processing entry point for Jersey container implementations.
     *
     * @param request the Jersey {@link ContainerRequest} to process
     */
    void handle(ContainerRequest request);

}
