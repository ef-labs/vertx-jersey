package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import org.glassfish.jersey.server.ContainerRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;

import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/9/13
 * Time: 7:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CustomSecurityContextProvider implements VertxRequestProcessor {


    private final Vertx vertx;

    @Inject
    public CustomSecurityContextProvider(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Provide additional async request processing
     *
     * @param vertxRequest  the vert.x http server request
     * @param jerseyRequest the jersey container request
     * @param done          the done async callback handler
     */
    @Override
    public void process(final HttpServerRequest vertxRequest, final ContainerRequest jerseyRequest, final Handler<Void> done) {

        vertx.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                SecurityContext securityContext = new SecurityContext() {
                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public Principal getUserPrincipal() {
                        return null;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isUserInRole(String role) {
                        String param = vertxRequest.params().get("role");
                        return role.equalsIgnoreCase(param);
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    public String getAuthenticationScheme() {
                        return null;
                    }
                };

                jerseyRequest.setSecurityContext(securityContext);
                done.handle(null);
            }
        });

    }
}
