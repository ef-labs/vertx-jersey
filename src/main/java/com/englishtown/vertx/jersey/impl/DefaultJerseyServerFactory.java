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
import com.englishtown.vertx.jersey.JerseyServerFactory;
import org.glassfish.hk2.api.Factory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Default implementation of {@link com.englishtown.vertx.jersey.JerseyServerFactory}
 */
public class DefaultJerseyServerFactory implements JerseyServerFactory {

    private final Provider<JerseyHandler> jerseyHandlerProvider;
    private final Factory<Ref<Vertx>> vertxReferencingFactory;
    private final Factory<Ref<Container>> containerReferencingFactory;

    @Inject
    public DefaultJerseyServerFactory(
            Provider<JerseyHandler> jerseyHandlerProvider,
            Factory<Ref<Vertx>> vertxReferencingFactory,
            Factory<Ref<Container>> containerReferencingFactory) {
        this.jerseyHandlerProvider = jerseyHandlerProvider;
        this.vertxReferencingFactory = vertxReferencingFactory;
        this.containerReferencingFactory = containerReferencingFactory;
    }

    @Override
    public JerseyServer createServer() {
        return new DefaultJerseyServer(jerseyHandlerProvider, vertxReferencingFactory, containerReferencingFactory);
    }
}
