package com.englishtown.vertx.jersey;

import com.hazelcast.nio.FastByteArrayInputStream;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ContainerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 3/27/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class JerseyHandler implements Handler<HttpServerRequest> {

    private ApplicationHandler application;
    private final URI baseUri;

    public JerseyHandler(URI baseUri, ApplicationHandler application) {
        this.baseUri = baseUri;
        this.application = application;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final HttpServerRequest vertxRequest) {
        // Wait for body to arrive and pass on to jersey
        vertxRequest.bodyHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer body) {
                handleBody(vertxRequest, body);
            }
        });
    }

    private void handleBody(HttpServerRequest vertxRequest, Buffer body) {

        ContainerRequest jerseyRequest = new ContainerRequest(
                baseUri,
                vertxRequest.getAbsoluteURI(),
                vertxRequest.method,
                getSecurityContext(vertxRequest),
                new MapPropertiesDelegate());

        InputStream inputStream = new FastByteArrayInputStream(body.getBytes());
        jerseyRequest.setEntityStream(inputStream); // TODO: This could possibly be better...
        jerseyRequest.setWriter(new VertxResponseWriter(vertxRequest));

        for (Map.Entry<String, String> header : vertxRequest.headers().entrySet()) {
            jerseyRequest.getHeaders().add(header.getKey(), header.getValue());
        }

        application.handle(jerseyRequest);

    }

    // TODO: Need to be able inject principal provider
    private SecurityContext getSecurityContext(final HttpServerRequest request) {
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
