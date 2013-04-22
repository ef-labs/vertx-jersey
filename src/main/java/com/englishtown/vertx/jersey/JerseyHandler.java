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

import com.englishtown.vertx.jersey.inject.VertxRequestHandler;
import com.englishtown.vertx.jersey.security.SecurityContextProvider;
import com.hazelcast.nio.FastByteArrayInputStream;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Vertx {@link HttpServerRequest} handler that delegates handling to Jersey
 */
public class JerseyHandler implements Handler<HttpServerRequest> {

    public static final String PROPERTY_NAME_VERTX = "vertx.vertx";
    public static final String PROPERTY_NAME_CONTAINER = "vertx.container";
    public static final String PROPERTY_NAME_REQUEST = "vertx.request";
    public static final int DEFAULT_MAX_BODY_SIZE = 1024 * 1000; // Default max body size to 1MB

    private final Vertx vertx;
    private final Container container;
    private final ApplicationHandler application;
    private final URI baseUri;
    private final SecurityContextProvider securityContextProvider;
    private final List<VertxRequestHandler> vertxHandlers;
    private final int maxBodySize;

    public JerseyHandler(Vertx vertx, Container container, URI baseUri, ApplicationHandler application) {
        this(vertx, container, baseUri, application,
                application.getServiceLocator().getService(SecurityContextProvider.class),
                application.getServiceLocator().getAllServices(VertxRequestHandler.class));
    }

    @Inject
    public JerseyHandler(
            Vertx vertx,
            Container container,
            URI baseUri,
            ApplicationHandler application,
            SecurityContextProvider securityContextProvider,
            List<VertxRequestHandler> vertxHandlers) {
        this.vertx = vertx;
        this.container = container;
        this.baseUri = baseUri;
        this.application = application;
        this.securityContextProvider = securityContextProvider;
        this.vertxHandlers = vertxHandlers;
        this.maxBodySize = container.config().getNumber(JerseyModule.CONFIG_MAX_BODY_SIZE, DEFAULT_MAX_BODY_SIZE).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final HttpServerRequest vertxRequest) {

        // Get the security context
        securityContextProvider.getSecurityContext(vertxRequest, new Handler<SecurityContext>() {
            @Override
            public void handle(final SecurityContext securityContext) {
                JerseyHandler.this.handle(vertxRequest, securityContext);
            }
        });

    }

    void handle(final HttpServerRequest vertxRequest, final SecurityContext securityContext) {

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
                    JerseyHandler.this.handle(vertxRequest, securityContext, inputStream);
                }
            });

        } else {
            JerseyHandler.this.handle(vertxRequest, securityContext, null);
        }

    }

    void handle(
            final HttpServerRequest vertxRequest,
            final SecurityContext securityContext,
            final InputStream inputStream
    ) {

        // Stick the vertx params in the properties bag
        final Map<String, Object> properties = new HashMap<>();
        properties.put(PROPERTY_NAME_VERTX, vertx);
        properties.put(PROPERTY_NAME_CONTAINER, container);
        properties.put(PROPERTY_NAME_REQUEST, vertxRequest);

        if (vertxHandlers != null && vertxHandlers.size() > 0) {
            callVertxHandler(0, vertxRequest, securityContext, inputStream, properties, new Handler<Void>() {
                @Override
                public void handle(Void aVoid) {
                    JerseyHandler.this.handle(vertxRequest, securityContext, inputStream, properties);
                }
            });
        } else {
            handle(vertxRequest, securityContext, inputStream, properties);
        }

    }

    void callVertxHandler(
            int index,
            final HttpServerRequest vertxRequest,
            final SecurityContext securityContext,
            final InputStream inputStream,
            final Map<String, Object> properties,
            final Handler<Void> done
    ) {

        if (index >= vertxHandlers.size()) {
            done.handle(null);
        }

        VertxRequestHandler handler = vertxHandlers.get(index);
        final int next = index + 1;

        handler.handle(vertxRequest, properties, new Handler<Void>() {
            @Override
            public void handle(Void aVoid) {
                if (next >= vertxHandlers.size()) {
                    done.handle(null);
                } else {
                    callVertxHandler(next, vertxRequest, securityContext, inputStream, properties, done);
                }
            }
        });

    }

    void handle(
            final HttpServerRequest vertxRequest,
            SecurityContext securityContext,
            InputStream inputStream,
            Map<String, Object> properties) {

        // Create the jersey request
        ContainerRequest jerseyRequest = new ContainerRequest(
                baseUri,
                vertxRequest.absoluteURI(),
                vertxRequest.method(),
                securityContext,
                new MapPropertiesDelegate(properties));

        // Provide the vertx response writer
        jerseyRequest.setWriter(new VertxResponseWriter(vertxRequest, vertx, container.logger()));

        // Set entity stream if provided (form posts)
        if (inputStream != null) {
            jerseyRequest.setEntityStream(inputStream);
        }

        // Copy headers
        for (Map.Entry<String, String> header : vertxRequest.headers().entrySet()) {
            jerseyRequest.getHeaders().add(header.getKey(), header.getValue());
        }

        application.handle(jerseyRequest);

    }

    boolean shouldReadData(HttpServerRequest vertxRequest) {

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
