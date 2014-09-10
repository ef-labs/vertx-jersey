package com.englishtown.vertx.jersey.examples.guice.inject;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Example guice implementation of {@link javax.ws.rs.container.ContainerResponseFilter}
 */
public class GuiceResponseFilter implements ContainerResponseFilter {
    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        if (responseContext != null) {

        }
    }
}
