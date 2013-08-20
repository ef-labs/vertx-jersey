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

package com.englishtown.vertx.jersey.inject;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.inject.ReferencingFactory;
import org.glassfish.jersey.internal.util.collection.Ref;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.streams.ReadStream;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * HK2 binder to configure the minimum interfaces required for the {@link com.englishtown.vertx.jersey.JerseyModule}
 * to start.
 */
public class InternalVertxJerseyBinder extends AbstractBinder {

    private final Vertx vertx;
    private final Container container;

    /**
     * Referencing factory for vert.x request.
     */
    private static class VertxRequestReferencingFactory extends ReferencingFactory<HttpServerRequest> {
        @Inject
        public VertxRequestReferencingFactory(Provider<Ref<HttpServerRequest>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * Referencing factory for vert.x response.
     */
    private static class VertxResponseReferencingFactory extends ReferencingFactory<HttpServerResponse> {
        @Inject
        public VertxResponseReferencingFactory(Provider<Ref<HttpServerResponse>> referenceFactory) {
            super(referenceFactory);
        }
    }

    static class VertxResponseProcessorFactory implements Factory<List<VertxResponseProcessor>> {

        private final List<VertxResponseProcessor> processors = new ArrayList<>();

        @Inject
        public VertxResponseProcessorFactory(IterableProvider<VertxResponseProcessor> providers) {
            for (VertxResponseProcessor processor : providers) {
                processors.add(processor);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<VertxResponseProcessor> provide() {
            return processors;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose(List<VertxResponseProcessor> instance) {
        }
    }

    static class VertxRequestProcessorFactory implements Factory<List<VertxRequestProcessor>> {

        private final List<VertxRequestProcessor> processors = new ArrayList<>();

        @Inject
        public VertxRequestProcessorFactory(IterableProvider<VertxRequestProcessor> providers) {
            for (VertxRequestProcessor processor : providers) {
                processors.add(processor);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<VertxRequestProcessor> provide() {
            return processors;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose(List<VertxRequestProcessor> instance) {
        }
    }

    public InternalVertxJerseyBinder(Vertx vertx, Container container) {
        this.vertx = vertx;
        this.container = container;
    }

    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        // Bind the vertx and container instances
        bind(vertx).to(Vertx.class);
        bind(container).to(Container.class);

        // Request and read stream
        bindFactory(VertxRequestReferencingFactory.class).to(HttpServerRequest.class).in(PerLookup.class);
        bindFactory(VertxRequestReferencingFactory.class).to(new TypeLiteral<ReadStream<HttpServerRequest>>() {
        }).in(PerLookup.class);
        bindFactory(ReferencingFactory.<HttpServerRequest>referenceFactory()).to(new TypeLiteral<Ref<HttpServerRequest>>() {
        }).in(RequestScoped.class);

        // Response
        bindFactory(VertxResponseReferencingFactory.class).to(HttpServerResponse.class).in(PerLookup.class);
        bindFactory(ReferencingFactory.<HttpServerResponse>referenceFactory()).to(new TypeLiteral<Ref<HttpServerResponse>>() {
        }).in(RequestScoped.class);

        bindFactory(VertxRequestProcessorFactory.class).to(new TypeLiteral<List<VertxRequestProcessor>>() {
        });
        bindFactory(VertxResponseProcessorFactory.class).to(new TypeLiteral<List<VertxResponseProcessor>>() {
        });

    }
}
