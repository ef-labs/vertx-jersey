/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey;

import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.security.DefaultSecurityContext;
import com.hazelcast.nio.FastByteArrayInputStream;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Vertx {@link HttpServerRequest} handler that delegates handling to Jersey
 */
public class DefaultJerseyHandler implements JerseyHandler {

    public static final String CONFIG_BASE_PATH = "base_path";
    public static final String CONFIG_MAX_BODY_SIZE = "max_body_size";
    public static final String CONFIG_RESOURCES = "resources";
    public static final String CONFIG_FEATURES = "features";
    public static final String CONFIG_BINDERS = "binders";
    public static final int DEFAULT_MAX_BODY_SIZE = 1024 * 1000; // Default max body size to 1MB
    public static final String PROPERTY_NAME_VERTX = "vertx.vertx";
    public static final String PROPERTY_NAME_CONTAINER = "vertx.container";
    public static final String PROPERTY_NAME_REQUEST = "vertx.request";

    private Vertx vertx;
    private Container container;
    private ApplicationHandler application;
    private URI baseUri;
    private final ContainerResponseWriterProvider responseWriterProvider;
    private int maxBodySize;
    private final List<VertxRequestProcessor> requestProcessors;

    @Inject
    public DefaultJerseyHandler(
            ContainerResponseWriterProvider responseWriterProvider,
            List<VertxRequestProcessor> requestProcessors) {
        this.responseWriterProvider = responseWriterProvider;
        this.requestProcessors = requestProcessors != null ? requestProcessors : new ArrayList<VertxRequestProcessor>();
    }

    @Override
    public void init(Vertx vertx, Container container) {

        JsonObject config = container.config();
        if (config == null) {
            throw new IllegalStateException("The vert.x container configuration is null");
        }

        this.vertx = vertx;
        this.container = container;
        this.baseUri = getBaseUri(config);
        this.application = getApplicationHandler(config);
        this.maxBodySize = getMaxBodySize(config);

    }

    protected URI getBaseUri(JsonObject config) {
        String basePath = config.getString(CONFIG_BASE_PATH, "/");

        // TODO: Does basePath need the trailing "/"?
//        if (!basePath.endsWith("/")) {
//            basePath += "/";
//        }

        return URI.create(basePath);
    }

    protected ApplicationHandler getApplicationHandler(JsonObject config) {
        ResourceConfig rc = getResourceConfig(config);
        return new ApplicationHandler(rc);
    }

    protected ResourceConfig getResourceConfig(JsonObject config) {

        JsonArray resources = config.getArray(CONFIG_RESOURCES, null);

        if (resources == null || resources.size() == 0) {
            throw new RuntimeException("At lease one resource package name must be specified in the config " +
                    CONFIG_RESOURCES);
        }

        String[] resourceArr = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            resourceArr[i] = String.valueOf(resources.get(i));
        }

        ResourceConfig rc = new ResourceConfig();
        rc.packages(resourceArr);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        JsonArray features = config.getArray(CONFIG_FEATURES, null);
        if (features != null && features.size() > 0) {
            for (int i = 0; i < features.size(); i++) {
                try {
                    Class<?> clazz = cl.loadClass(String.valueOf(features.get(i)));
                    rc.register(clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        JsonArray binders = config.getArray(CONFIG_BINDERS, null);
        if (binders != null && binders.size() > 0) {
            for (int i = 0; i < binders.size(); i++) {
                try {
                    Class<?> clazz = cl.loadClass(String.valueOf(binders.get(i)));
                    rc.register(clazz.newInstance());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return rc;

    }

    protected int getMaxBodySize(JsonObject config) {
        return config.getNumber(CONFIG_MAX_BODY_SIZE, DEFAULT_MAX_BODY_SIZE).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final HttpServerRequest vertxRequest) {

        // Wait for the body for jersey to handle form/json/xml params
        if (shouldReadData(vertxRequest)) {
            final Buffer body = new Buffer();

            vertxRequest.dataHandler(new Handler<Buffer>() {
                public void handle(Buffer buff) {
                    body.appendBuffer(buff);
                    if (body.length() > maxBodySize) {
                        throw new RuntimeException("The input stream has exceeded the max allowed body size "
                                + maxBodySize + ".");
                    }
                }
            });
            vertxRequest.endHandler(new Handler<Void>() {
                @Override
                public void handle(Void event) {
                    InputStream inputStream = new FastByteArrayInputStream(body.getBytes());
                    DefaultJerseyHandler.this.handle(vertxRequest, inputStream);
                }
            });

        } else {
            DefaultJerseyHandler.this.handle(vertxRequest, null);
        }

    }

    protected void handle(
            final HttpServerRequest vertxRequest,
            final InputStream inputStream
    ) {

        // Stick the vertx params in the properties bag
        final Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_NAME_VERTX, vertx);
        properties.put(PROPERTY_NAME_CONTAINER, container);
        properties.put(PROPERTY_NAME_REQUEST, vertxRequest);

        String scheme = vertxRequest.absoluteURI() == null ? null : vertxRequest.absoluteURI().getScheme();
        boolean isSecure = "https".equalsIgnoreCase(scheme);

        // Create the jersey request
        final ContainerRequest jerseyRequest = new ContainerRequest(
                baseUri,
                vertxRequest.absoluteURI(),
                vertxRequest.method(),
                new DefaultSecurityContext(isSecure),
                new MapPropertiesDelegate(properties));

        // Provide the vertx response writer
        jerseyRequest.setWriter(responseWriterProvider.get(vertxRequest, jerseyRequest));

        // Set entity stream if provided (form posts)
        if (inputStream != null) {
            jerseyRequest.setEntityStream(inputStream);
        }

        // Copy headers
        for (Map.Entry<String, String> header : vertxRequest.headers().entrySet()) {
            jerseyRequest.getHeaders().add(header.getKey(), header.getValue());
        }

        // Call vertx before request processors
        if (requestProcessors.size() > 0) {
            callVertxRequestProcessor(0, vertxRequest, jerseyRequest, new Handler<Void>() {
                @Override
                public void handle(Void aVoid) {
                    DefaultJerseyHandler.this.handle(jerseyRequest);
                }
            });
        } else {
            handle(jerseyRequest);
        }

    }

    void callVertxRequestProcessor(
            int index,
            final HttpServerRequest vertxRequest,
            final ContainerRequest jerseyRequest,
            final Handler<Void> done
    ) {

        if (index >= requestProcessors.size()) {
            done.handle(null);
        }

        VertxRequestProcessor handler = requestProcessors.get(index);
        final int next = index + 1;

        handler.handle(vertxRequest, jerseyRequest, new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
                if (next >= requestProcessors.size()) {
                    done.handle(null);
                } else {
                    callVertxRequestProcessor(next, vertxRequest, jerseyRequest, done);
                }
            }
        });

    }

    void handle(ContainerRequest jerseyRequest) {
        application.handle(jerseyRequest);
    }

    protected boolean shouldReadData(HttpServerRequest vertxRequest) {

        String method = vertxRequest.method();

        // Only read input stream data for post/put methods
        if (!(HttpMethod.POST.equalsIgnoreCase(method) || HttpMethod.PUT.equalsIgnoreCase(method))) {
            return false;
        }

        String contentType = vertxRequest.headers().get(HttpHeaders.CONTENT_TYPE);

        // Only read input stream data for content types form/json/xml
        return MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(contentType)
                || MediaType.APPLICATION_JSON.equalsIgnoreCase(contentType)
                || MediaType.APPLICATION_XML.equalsIgnoreCase(contentType);
    }

}
