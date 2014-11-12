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

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * The Vertx Module to enable Jersey to handle JAX-RS resources
 */
public class JerseyModule extends AbstractVerticle {

    private final Provider<JerseyConfigurator> configuratorProvider;
    private final Provider<JerseyServer> jerseyServerProvider;
    private JerseyServer jerseyServer;

    @Inject
    public JerseyModule(Provider<JerseyServer> jerseyServerProvider, Provider<JerseyConfigurator> configuratorProvider) {
        this.jerseyServerProvider = jerseyServerProvider;
        this.configuratorProvider = configuratorProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final Future<Void> startedResult) throws Exception {
        this.start();

        JsonObject config = getVertx().context().config();
        jerseyServer = jerseyServerProvider.get();
        JerseyConfigurator configurator = configuratorProvider.get();

        configurator.init(config, getVertx());

        jerseyServer.init(configurator, ar -> {
            if (ar.succeeded()) {
                startedResult.complete();
            } else {
                startedResult.fail(ar.cause());
            }
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        jerseyServer.close();
        jerseyServer = null;
    }
}
