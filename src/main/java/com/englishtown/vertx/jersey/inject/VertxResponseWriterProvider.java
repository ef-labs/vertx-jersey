package com.englishtown.vertx.jersey.inject;

import com.englishtown.vertx.jersey.VertxResponseWriter;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of ContainerResponseWriterProvider
 */
@Singleton
public class VertxResponseWriterProvider implements ContainerResponseWriterProvider {

    private final Vertx vertx;
    private final Container container;
    private final List<VertxResponseProcessor> responseProcessors;

    @Inject
    public VertxResponseWriterProvider(Vertx vertx, Container container, List<VertxResponseProcessor> responseProcessors) {
        this.vertx = vertx;
        this.container = container;
        this.responseProcessors = responseProcessors != null ? responseProcessors : new ArrayList<VertxResponseProcessor>();
    }

    @Override
    public ContainerResponseWriter get(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest) {
        return new VertxResponseWriter(vertxRequest, vertx, container.logger(), responseProcessors);
    }

}
