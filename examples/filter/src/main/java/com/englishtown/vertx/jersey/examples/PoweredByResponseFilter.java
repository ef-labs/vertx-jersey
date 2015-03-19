package com.englishtown.vertx.jersey.examples;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Example Jersey Filter borrowed from:
 * https://jersey.java.net/documentation/latest/filters-and-interceptors.html#d0e8019
 */
public class PoweredByResponseFilter implements ContainerResponseFilter {

    public static final String HEADER_X_POWERED_BY = "X-Powered-By";
    public static final String HEADER_X_POWERED_BY_VALUE = "Jersey :-)";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        responseContext.getHeaders().add(HEADER_X_POWERED_BY, HEADER_X_POWERED_BY_VALUE);
    }
}
