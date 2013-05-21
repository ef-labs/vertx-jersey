package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;

/**
 * Default {@link com.englishtown.vertx.jersey.ApplicationHandlerDelegate} implementation
 */
public class DefaultApplicationHandlerDelegate implements ApplicationHandlerDelegate {

    private final ApplicationHandler delegate;

    public DefaultApplicationHandlerDelegate(ApplicationHandler handler) {
        delegate = handler;
    }

    /**
     * The main request/response processing entry point for Jersey container implementations.
     *
     * @param request the Jersey {@link org.glassfish.jersey.server.ContainerRequest} to process
     */
    @Override
    public void handle(ContainerRequest request) {
        delegate.handle(request);
    }
}
