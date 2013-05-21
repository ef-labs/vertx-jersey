package com.englishtown.vertx.jersey.inject.impl;

import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.junit.Test;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Container;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * {@link VertxResponseWriterProvider} unit tests
 */
public class VertxResponseWriterProviderTest {
    @Test
    public void testGet() throws Exception {

        Vertx vertx = mock(Vertx.class);
        Container container = mock(Container.class);
        List<VertxResponseProcessor> responseProcessors = new ArrayList<>();
        VertxResponseWriterProvider provider = new VertxResponseWriterProvider(vertx, container, responseProcessors);
        HttpServerRequest vertxRequest = mock(HttpServerRequest.class);
        ContainerRequest jerseyRequest = mock(ContainerRequest.class);
        ContainerResponseWriter writer;

        writer = provider.get(vertxRequest, jerseyRequest);
        assertNotNull(writer);
    }
}
