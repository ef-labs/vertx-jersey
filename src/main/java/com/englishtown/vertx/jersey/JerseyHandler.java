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

import com.englishtown.vertx.jersey.security.SecurityContextProvider;
import com.hazelcast.nio.FastByteArrayInputStream;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ResourceConfig;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Container;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * The Vertx {@link HttpServerRequest} handler that delegates handling to Jersey
 */
public class JerseyHandler implements Handler<HttpServerRequest> {

    public static final String PROPERTY_NAME_VERTX = "vertx.vertx";
    public static final String PROPERTY_NAME_CONTAINER = "vertx.container";
    public static final String PROPERTY_NAME_REQUEST = "vertx.request";

    private final Vertx vertx;
    private final Container container;
    private final ApplicationHandler application;
    private final URI baseUri;
    private final SecurityContextProvider securityContextProvider;

    public JerseyHandler(Vertx vertx, Container container, URI baseUri, ResourceConfig rc) {
        this.vertx = vertx;
        this.container = container;
        this.baseUri = baseUri;
        this.application = new ApplicationHandler(rc);
        this.securityContextProvider = this.application.getServiceLocator().getService(SecurityContextProvider.class);
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

        // If this is a form post, we need to wait for the body for jersey to handle form params
        if (isFormPost(vertxRequest)) {
            vertxRequest.bodyHandler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer body) {
                    // TODO: Use dataHandler instead and limit body size to something reasonable
                    InputStream inputStream = new FastByteArrayInputStream(body.getBytes());
                    JerseyHandler.this.handle(vertxRequest, securityContext, inputStream);
                }
            });
        } else {
            JerseyHandler.this.handle(vertxRequest, securityContext, null);
        }

    }

    void handle(final HttpServerRequest vertxRequest, SecurityContext securityContext, InputStream inputStream) {

        // Stick the vertx params in the properties bag
        MapPropertiesDelegate properties = new MapPropertiesDelegate();
        properties.setProperty(PROPERTY_NAME_VERTX, vertx);
        properties.setProperty(PROPERTY_NAME_CONTAINER, container);
        properties.setProperty(PROPERTY_NAME_REQUEST, vertxRequest);

        // Create the jersey request
        ContainerRequest jerseyRequest = new ContainerRequest(
                baseUri,
                vertxRequest.getAbsoluteURI(),
                vertxRequest.method,
                securityContext,
                properties);

        // Provide the vertx response writer
        jerseyRequest.setWriter(new VertxResponseWriter(vertxRequest));

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

    boolean isFormPost(HttpServerRequest vertxRequest) {
        return "POST".equalsIgnoreCase(vertxRequest.method)
                && MediaType.APPLICATION_FORM_URLENCODED.equalsIgnoreCase(vertxRequest.headers().get("Content-Type"));
    }

}
