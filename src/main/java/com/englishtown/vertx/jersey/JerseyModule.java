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

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Future;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * The Vertx Module to enable Jersey to handle JAX-RS resources
 */
public class JerseyModule extends BusModBase {

    final static String CONFIG_HOST = "host";
    final static String CONFIG_PORT = "port";
    final static String CONFIG_RECEIVE_BUFFER_SIZE = "receive_buffer_size";

    private final Provider<JerseyHandler> jerseyHandlerProvider;

    @Inject
    public JerseyModule(Provider<JerseyHandler> jerseyHandlerProvider) {
        this.jerseyHandlerProvider = jerseyHandlerProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Future<Void> startedResult) {
        this.start();

        // Get http server config values
        final String host = getOptionalStringConfig(CONFIG_HOST, "0.0.0.0");
        final int port = getOptionalIntConfig(CONFIG_PORT, 80);
        int receiveBufferSize = getOptionalIntConfig(CONFIG_RECEIVE_BUFFER_SIZE, 0);

        // Create http server
        HttpServer server = vertx.createHttpServer();

        // Create jersey handler and set as request handler
        JerseyHandler handler = jerseyHandlerProvider.get();
        if (handler == null) {
            throw new IllegalStateException("A JerseyHandlerProvider has not been configured");
        }
        handler.init(vertx, container);
        server.requestHandler(handler);

        if (receiveBufferSize > 0) {
            // TODO: This doesn't seem to actually affect buffer size for dataHandler.  Is this being used correctly or is it a Vertx bug?
            server.setReceiveBufferSize(receiveBufferSize);
        }

        // Start listening and log success/failure
        server.listen(port, host, new Handler<AsyncResult<HttpServer>>() {
            @Override
            public void handle(AsyncResult<HttpServer> result) {
                final String listenPath = "http://" + host + ":" + port;
                if (result.succeeded()) {
                    container.logger().info("Http server listening for " + listenPath);
                    startedResult.setResult(null);
                } else {
                    container.logger().error("Failed to start http server listening for " + listenPath, result.cause());
                    startedResult.setFailure(result.cause());
                }
            }
        });

    }

}
