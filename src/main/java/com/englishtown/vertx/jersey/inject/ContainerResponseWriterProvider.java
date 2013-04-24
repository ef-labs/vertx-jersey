package com.englishtown.vertx.jersey.inject;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Injectable interface to provide the Jersey ContainerResponseWriter instance for the current request
 */
public interface ContainerResponseWriterProvider {

    public ContainerResponseWriter get(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest);

}
