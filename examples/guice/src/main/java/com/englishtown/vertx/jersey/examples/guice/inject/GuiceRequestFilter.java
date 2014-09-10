package com.englishtown.vertx.jersey.examples.guice.inject;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * Example guice implementation of {@link javax.ws.rs.container.ContainerRequestFilter}
 */
public class GuiceRequestFilter implements ContainerRequestFilter {
    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext != null) {

        }
    }
}
