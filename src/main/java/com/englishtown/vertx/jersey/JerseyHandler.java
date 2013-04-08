package com.englishtown.vertx.jersey;

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
import java.security.Principal;
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
    private ApplicationHandler application;
    private final URI baseUri;

    public JerseyHandler(Vertx vertx, Container container, URI baseUri, ResourceConfig rc) {
        this.vertx = vertx;
        this.container = container;
        this.baseUri = baseUri;
        this.application = new ApplicationHandler(rc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final HttpServerRequest vertxRequest) {

        // If this is a form post, we need to wait for the body for jersey to handle form params
        if (isFormPost(vertxRequest)) {
            vertxRequest.bodyHandler(new Handler<Buffer>() {
                @Override
                public void handle(Buffer body) {
                    // TODO: Use dataHandler instead and limit body size to something reasonable
                    InputStream inputStream = new FastByteArrayInputStream(body.getBytes());
                    JerseyHandler.this.handle(vertxRequest, inputStream);
                }
            });
        } else {
            handle(vertxRequest, null);
        }

    }

    void handle(final HttpServerRequest vertxRequest, InputStream inputStream) {

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
                getSecurityContext(vertxRequest),
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

    // TODO: Need to be able inject SecurityContext provider
    SecurityContext getSecurityContext(final HttpServerRequest request) {
        return new SecurityContext() {

            @Override
            public boolean isUserInRole(String role) {
                return false;
            }

            @Override
            public boolean isSecure() {
                return request.getAbsoluteURI().getScheme().equalsIgnoreCase("https");
            }

            @Override
            public Principal getUserPrincipal() {
                return null;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
        };
    }
}
