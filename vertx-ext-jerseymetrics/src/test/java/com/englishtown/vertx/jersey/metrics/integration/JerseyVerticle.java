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

package com.englishtown.vertx.jersey.metrics.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import javax.inject.Inject;
import javax.inject.Provider;

import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyServer;

/**
 * Test verticle to start jersey
 */
public class JerseyVerticle extends AbstractVerticle {

    private final Provider<JerseyServer> serverProvider;
    private final Provider<JerseyConfigurator> configuratorProvider;

    @Inject
    public JerseyVerticle(Provider<JerseyServer> serverProvider, Provider<JerseyConfigurator> configuratorProvider) {
        this.serverProvider = serverProvider;
        this.configuratorProvider = configuratorProvider;
    }

    /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     */
    @Override
    public void start(final Future<Void> startedResult) {

        JerseyConfigurator configurator = configuratorProvider.get();
        configurator.init(getVertx().context().config(), getVertx());

        JerseyServer server = serverProvider.get();
        server.init(configurator, ar -> { 
            if (ar.succeeded()) {
                //TODO Migration: Start unnessary?
                //start();
                startedResult.complete();
            } else {
                startedResult.fail(ar.cause());
            }
        });

    }
}
