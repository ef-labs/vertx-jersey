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

import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServer;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Default implementation of {@link JerseyServer}
 */
public class DefaultJerseyServer implements JerseyServer {

    final static String CONFIG_HOST = "host";
    final static String CONFIG_PORT = "port";
    final static String CONFIG_RECEIVE_BUFFER_SIZE = "receive_buffer_size";

    private final JerseyHandler jerseyHandler;
    private final Factory<Ref<Vertx>> vertxReferencingFactory;
    private final Factory<Ref<Container>> containerReferencingFactory;
    private Handler<RouteMatcher> routeMatcherHandler;

    @Inject
    public DefaultJerseyServer(
            Provider<JerseyHandler> jerseyHandlerProvider,
            Factory<Ref<Vertx>> vertxReferencingFactory,
            Factory<Ref<Container>> containerReferencingFactory) {
        this.jerseyHandler = jerseyHandlerProvider.get();
        if (jerseyHandler == null) {
            throw new IllegalStateException("The JerseyHandler provider returned null");
        }
        this.vertxReferencingFactory = vertxReferencingFactory;
        this.containerReferencingFactory = containerReferencingFactory;
    }

    @Override
    public void init(JsonObject config, Vertx vertx, final Container container) {
        init(config, vertx, container, null);
    }

    @Override
    public void init(JsonObject config, Vertx vertx, final Container container, final Handler<AsyncResult<HttpServer>> doneHandler) {

        // Store vertx/container instances
        vertxReferencingFactory.provide().set(vertx);
        containerReferencingFactory.provide().set(container);

        // Get http server config values
        final String host = config.getString(CONFIG_HOST, "0.0.0.0");
        final int port = config.getInteger(CONFIG_PORT, 80);
        int receiveBufferSize = config.getInteger(CONFIG_RECEIVE_BUFFER_SIZE, 0);

        // Create http server
        HttpServer server = vertx.createHttpServer();

        // Init jersey handler
        jerseyHandler.init(vertx, container);

        if (receiveBufferSize > 0) {
            // TODO: This doesn't seem to actually affect buffer size for dataHandler.  Is this being used correctly or is it a Vertx bug?
            server.setReceiveBufferSize(receiveBufferSize);
        }

        // Set request handler, use route matcher if a route handler is provided.
        if (routeMatcherHandler == null) {
            server.requestHandler(jerseyHandler);
        } else {
            RouteMatcher rm = new RouteMatcher();
            String pattern = jerseyHandler.getBaseUri().getPath() + "*";
            rm.all(pattern, jerseyHandler);
            routeMatcherHandler.handle(rm);
        }

        // Start listening and log success/failure
        server.listen(port, host, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> result) {
                final String listenPath = "http://" + host + ":" + port;
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
}
