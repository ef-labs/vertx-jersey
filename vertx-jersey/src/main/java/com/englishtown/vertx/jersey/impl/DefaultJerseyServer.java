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
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.VertxContainer;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.glassfish.hk2.api.ServiceLocatorFactory;

import javax.inject.Inject;

/**
 * Default implementation of {@link JerseyServer}
 */
public class DefaultJerseyServer implements JerseyServer {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJerseyServer.class);

    private JerseyHandler jerseyHandler;
    private VertxContainer container;
    private Handler<HttpServer> setupHandler;
    private HttpServer server;

    @Inject
    public DefaultJerseyServer(JerseyHandler jerseyHandler, VertxContainer container) {
        this.jerseyHandler = jerseyHandler;
        this.container = container;
    }

    @Override
    public void init(
            JerseyOptions options,
            Handler<AsyncResult<HttpServer>> doneHandler) {

        // Setup the http server options
        HttpServerOptions serverOptions = new HttpServerOptions()
                .setHost(options.getHost())
                .setPort(options.getPort())
                .setAcceptBacklog(options.getAcceptBacklog()) // Performance tweak
                .setCompressionSupported(options.getCompressionSupported());

        // Enable https
        if (options.getSSL()) {
            serverOptions.setSsl(true);
        }
        if (options.getKeyStoreOptions() != null) {
            serverOptions.setKeyStoreOptions(options.getKeyStoreOptions());
        }

        Integer receiveBufferSize = options.getReceiveBufferSize();
        if (receiveBufferSize != null && receiveBufferSize > 0) {
            // TODO: This doesn't seem to actually affect buffer size for dataHandler.  Is this being used correctly or is it a Vertx bug?
            serverOptions.setReceiveBufferSize(receiveBufferSize);
        }

        // Create the http server
        server = container.getVertx().createHttpServer(serverOptions);

        // Init container and handler
        container.init(options);
        jerseyHandler.init(container);

        // Set request handler for the baseUri
        server.requestHandler(jerseyHandler::handle);

        // Perform any additional server setup (add routes etc.)
        if (setupHandler != null) {
            setupHandler.handle(server);
        }

        // Start listening and log success/failure
        server.listen(ar -> {
            final String listenPath = (options.getSSL() ? "https" : "http") + "://" + serverOptions.getHost() + ":" + serverOptions.getPort();
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
        // Run jersey shutdown lifecycle
        if (container != null) {
            container.getApplicationHandler().onShutdown(container);
            container = null;
        }
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
