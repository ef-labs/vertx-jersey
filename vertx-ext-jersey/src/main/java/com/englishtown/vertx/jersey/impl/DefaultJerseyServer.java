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

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.routematcher.RouteMatcher;
import io.vertx.ext.routematcher.impl.RouteMatcherImpl;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.hk2.api.ServiceLocatorFactory;

import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServer;

/**
 * Default implementation of {@link JerseyServer}
 */
public class DefaultJerseyServer implements JerseyServer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJerseyServer.class);

    private final Provider<JerseyHandler> jerseyHandlerProvider;
    private JerseyHandler jerseyHandler;
    private Handler<RouteMatcher> routeMatcherHandler;
    private Handler<HttpServer> setupHandler;
    private HttpServer server;

    @Inject
    public DefaultJerseyServer(Provider<JerseyHandler> jerseyHandlerProvider) {
        this.jerseyHandlerProvider = jerseyHandlerProvider;
    }

    @Override
    public void init(
            final JerseyConfigurator configurator,
            final Handler<AsyncResult<HttpServer>> doneHandler) {

        this.jerseyHandler = jerseyHandlerProvider.get();
        if (jerseyHandler == null) {
            throw new IllegalStateException("The JerseyHandler provider returned null");
        }

        HttpServerOptions serverOptions = new HttpServerOptions();
        serverOptions.setHost(configurator.getHost());
        serverOptions.setPort(configurator.getPort());
        server = configurator.getVertx().createHttpServer(serverOptions);

        // Enable https
        if (configurator.getSSL()) {
            serverOptions.setSsl(true);
//          TODO Migration: Vert.x Interface for options not yet finished?
//          .setKeyStorePassword(configurator.getKeyStorePassword())
//          .setKeyStorePath(configurator.getKeyStorePath());
        }

        // Performance tweak
        serverOptions.setAcceptBacklog(configurator.getAcceptBacklog());

        Integer receiveBufferSize = configurator.getReceiveBufferSize();
        if (receiveBufferSize != null && receiveBufferSize > 0) {
            // TODO: This doesn't seem to actually affect buffer size for dataHandler.  Is this being used correctly or is it a Vertx bug?
            serverOptions.setReceiveBufferSize(receiveBufferSize);
        }

        // Init jersey handler
        jerseyHandler.init(configurator);

        // Set request handler for the baseUri
        RouteMatcher rm = new RouteMatcherImpl();
        server.requestHandler(request -> {
            rm.accept(request);
        });
        // regex pattern will be: "^base_path/.*"
        String pattern = "^" + jerseyHandler.getBaseUri().getPath() + ".*";
        rm.all(pattern, jerseyHandler);

        // Add any additional routes if handler is provided
        if (routeMatcherHandler != null) {
            routeMatcherHandler.handle(rm);
        }

        if (setupHandler != null) {
            setupHandler.handle(server);
        }

        // Start listening and log success/failure
        server.listen(ar -> {
            final String listenPath = (configurator.getSSL() ? "https" : "http") + "://" + serverOptions.getHost() + ":" + serverOptions.getPort();
            if (ar.succeeded()) {
                logger.info("Http server listening for " + listenPath);
            } else {
                logger.error("Failed to start http server listening for " + listenPath, ar.cause());
            }
            if (doneHandler != null) {
                doneHandler.handle(ar);
            }
        });

    }

    @Override
    public void routeMatcherHandler(Handler<RouteMatcher> handler) {
        this.routeMatcherHandler = handler;
    }

    /**
     * Allows custom setup during initialization before the http server is listening
     *
     * @param handler the handler invoked with the http server
     */
    @Override
    public void setupHandler(Handler<HttpServer> handler) {
     this.setupHandler = handler;
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

    /**
     * Returns the internal vert.x {@link io.vertx.core.http.HttpServer}
     *
     * @return the vert.x http server instance
     */
    @Override
    public HttpServer getHttpServer() {
        return server;
    }

    /**
     * Releases resources
     */
    @Override
    public void close() {
        // Destroy the jersey service locator
        if (jerseyHandler != null && jerseyHandler.getDelegate() != null) {
            ServiceLocatorFactory.getInstance().destroy(jerseyHandler.getDelegate().getServiceLocator());
            jerseyHandler = null;
        }
        if (server != null) {
            server.close();
            server = null;
        }
    }
}
