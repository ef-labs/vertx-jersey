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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Provider;

import com.englishtown.promises.Deferred;
import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.promises.WhenJerseyServer;

/**
 * Default implementation of {@link WhenJerseyServer}
 */
public class DefaultWhenJerseyServer implements WhenJerseyServer {

    private final Vertx vertx;
    private final Provider<JerseyServer> jerseyServerProvider;
    private final Provider<JerseyConfigurator> configuratorProvider;
    private final When when;

    @Inject
    public DefaultWhenJerseyServer(Vertx vertx, Provider<JerseyServer> jerseyServerProvider, Provider<JerseyConfigurator> configuratorProvider, When when) {
        this.vertx = vertx;
        this.jerseyServerProvider = jerseyServerProvider;
        this.configuratorProvider = configuratorProvider;
        this.when = when;
    }

    @Override
    public Promise<JerseyServer> createServer(JsonObject config) {
        final Deferred<JerseyServer> d = when.defer();

        try {
            final JerseyServer jerseyServer = jerseyServerProvider.get();
            JerseyConfigurator configurator = configuratorProvider.get();

            configurator.init(config, vertx);

            jerseyServer.init(configurator, result -> {
                if (result.succeeded()) {
                    d.resolve(jerseyServer);
                } else {
                    d.reject(result.cause());
                }
            });

        } catch (RuntimeException e) {
            d.reject(e);
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

        return createServer(config).then(jerseyServer -> when.resolve(""));

    }

}
