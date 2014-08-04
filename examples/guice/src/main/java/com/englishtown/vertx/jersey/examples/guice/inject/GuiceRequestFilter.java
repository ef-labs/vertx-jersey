package com.englishtown.vertx.jersey.examples.guice.inject;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * Created by adriangonzalez on 7/21/14.
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
