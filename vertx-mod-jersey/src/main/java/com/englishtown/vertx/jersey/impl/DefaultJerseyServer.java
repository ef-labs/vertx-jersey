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

package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServer;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Default implementation of {@link JerseyServer}
 */
public class DefaultJerseyServer implements JerseyServer {

    private final JerseyHandler jerseyHandler;
    private Handler<RouteMatcher> routeMatcherHandler;

    @Inject
    public DefaultJerseyServer(Provider<JerseyHandler> jerseyHandlerProvider) {
        this.jerseyHandler = jerseyHandlerProvider.get();
        if (jerseyHandler == null) {
            throw new IllegalStateException("The JerseyHandler provider returned null");
        }
    }

    @Override
    public void init(JerseyConfigurator configurator) {
        init(configurator, null);
    }

    @Override
    public void init(
            final JerseyConfigurator configurator,
            final Handler<AsyncResult<HttpServer>> doneHandler) {

        // Create http server
        HttpServer server = configurator.getVertx().createHttpServer();

        // Enable https
        if (configurator.getSSL()) {
            server.setSSL(true)
                    .setKeyStorePassword(configurator.getKeyStorePassword())
                    .setKeyStorePath(configurator.getKeyStorePath());
        }

        // Performance tweak
        server.setAcceptBacklog(configurator.getAcceptBacklog());

        Integer receiveBufferSize = configurator.getReceiveBufferSize();
        if (receiveBufferSize != null && receiveBufferSize > 0) {
            // TODO: This doesn't seem to actually affect buffer size for dataHandler.  Is this being used correctly or is it a Vertx bug?
            server.setReceiveBufferSize(receiveBufferSize);
        }

        // Init jersey handler
        jerseyHandler.init(configurator);

        // Set request handler for the baseUri
        RouteMatcher rm = new RouteMatcher();
        server.requestHandler(rm);
        String pattern = "^" + jerseyHandler.getBaseUri().getPath();
        if (pattern.endsWith("/")) {
            rm.all(pattern + ".*", jerseyHandler);
        } else {
            rm.all(pattern + "$", jerseyHandler);
            rm.all(pattern + "/.*", jerseyHandler);
        }

        // Add any additional routes if handler is provided
        if (routeMatcherHandler != null) {
            routeMatcherHandler.handle(rm);
        }

        final String host = configurator.getHost();
        final int port = configurator.getPort();
        final Container container = configurator.getContainer();

        // Start listening and log success/failure
        server.listen(port, host, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> result) {
                final String listenPath = (configurator.getSSL() ? "https" : "http") + "://" + host + ":" + port;
                if (result.succeeded()) {
                    container.logger().info("Http server listening for " + listenPath);
                } else {
                    container.logger().error("Failed to start http server listening for " + listenPath, result.cause());
                }
                if (doneHandler != null) {
                    doneHandler.handle(result);
                }
            }
        });

    }

    @Override
    public void routeMatcherHandler(Handler<RouteMatcher> handler) {
        this.routeMatcherHandler = handler;
    }

    /**
     * Returns the JerseyHandler instance for the JerseyServer
     *
     * @return the JerseyHandler instance
     */
    @Override
    public JerseyHandler getHandler() {
        return jerseyHandler;
    }
}
