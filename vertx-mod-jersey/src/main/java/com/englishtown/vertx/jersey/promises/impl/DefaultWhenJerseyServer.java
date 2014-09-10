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

package com.englishtown.vertx.jersey.promises.impl;

import com.englishtown.promises.*;
import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.promises.WhenJerseyServer;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Default implementation of {@link WhenJerseyServer}
 */
public class DefaultWhenJerseyServer implements WhenJerseyServer {

    private final Vertx vertx;
    private final Container container;
    private final Provider<JerseyServer> jerseyServerProvider;
    private final Provider<JerseyConfigurator> configuratorProvider;

    @Inject
    public DefaultWhenJerseyServer(Vertx vertx, Container container, Provider<JerseyServer> jerseyServerProvider, Provider<JerseyConfigurator> configuratorProvider) {
        this.vertx = vertx;
        this.container = container;
        this.jerseyServerProvider = jerseyServerProvider;
        this.configuratorProvider = configuratorProvider;
    }

    @Override
    public Promise<JerseyServer> createServer(JsonObject config) {
        final Deferred<JerseyServer> d = new When<JerseyServer>().defer();

        try {
            final JerseyServer jerseyServer = jerseyServerProvider.get();
            JerseyConfigurator configurator = configuratorProvider.get();

            configurator.init(config, vertx, container);

            jerseyServer.init(configurator, new Handler<AsyncResult<HttpServer>>() {
                @Override
                public void handle(AsyncResult<HttpServer> result) {
                    if (result.succeeded()) {
                        d.getResolver().resolve(jerseyServer);
                    } else {
                        Value<JerseyServer> value = (result.cause() == null)
                                ? new Value<>(jerseyServer)
                                : new Value<>(jerseyServer, result.cause());
                        d.getResolver().reject(value);
                    }
                }
            });

        } catch (RuntimeException e) {
            d.getResolver().reject(new Value<JerseyServer>(null, e));
        }

        return d.getPromise();
    }

    /**
     * Returns a promise for asynchronously creating a {@link com.englishtown.vertx.jersey.JerseyServer}.
     * The promise type matches the WhenContainer signature to facilitate parallel deployments.
     *
     * @param config the jersey json configuration
     * @return a promise for an empty string
     */
    @Override
    public Promise<String> createServerSimple(JsonObject config) {
        final Deferred<String> d = new When<String>().defer();

        createServer(config).then(
                new FulfilledRunnable<JerseyServer>() {
                    @Override
                    public Promise<JerseyServer> run(JerseyServer jerseyServer) {
                        d.getResolver().resolve("");
                        return null;
                    }
                },
                new RejectedRunnable<JerseyServer>() {
                    @Override
                    public Promise<JerseyServer> run(Value<JerseyServer> value) {
                        d.getResolver().reject(value.getCause());
                        return null;
                    }
                }
        );

        return d.getPromise();
    }

}
