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

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyHandlerConfigurator;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandlerConfigurator;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServer;
import com.englishtown.vertx.jersey.inject.impl.VertxResponseWriterProvider;
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
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * HK2 binder to configure the minimum interfaces required for the {@link com.englishtown.vertx.jersey.JerseyModule}
 * to start.
 */
public class VertxJerseyBinder extends AbstractBinder {

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
     * Referencing factory for vert.x vertx instance.
     */
    private static class VertxReferencingFactory extends ReferencingFactory<Vertx> {
        @Inject
        public VertxReferencingFactory(Provider<Ref<Vertx>> referenceFactory) {
            super(referenceFactory);
        }
    }

    /**
     * Referencing factory for vert.x container.
     */
    private static class VertxContainerReferencingFactory extends ReferencingFactory<Container> {
        @Inject
        public VertxContainerReferencingFactory(Provider<Ref<Container>> referenceFactory) {
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

    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        bindFactory(VertxRequestProcessorFactory.class).to(new TypeLiteral<List<VertxRequestProcessor>>() {
        });
        bindFactory(VertxResponseProcessorFactory.class).to(new TypeLiteral<List<VertxResponseProcessor>>() {
        });

        bind(DefaultApplicationHandlerDelegate.class).to(ApplicationHandlerDelegate.class);
        bind(DefaultJerseyServer.class).to(JerseyServer.class);
        bind(DefaultJerseyHandler.class).to(JerseyHandler.class);
        bind(DefaultJerseyHandlerConfigurator.class).to(JerseyHandlerConfigurator.class);
        bind(VertxResponseWriterProvider.class).to(ContainerResponseWriterProvider.class);

        // Vert.x instance
        bindFactory(VertxReferencingFactory.class).to(Vertx.class).in(PerLookup.class);
        bindFactory(ReferencingFactory.<Vertx>referenceFactory()).to(new TypeLiteral<Ref<Vertx>>() {
        }).in(Singleton.class);

        // Container
        bindFactory(VertxContainerReferencingFactory.class).to(Container.class).in(PerLookup.class);
        bindFactory(ReferencingFactory.<Container>referenceFactory()).to(new TypeLiteral<Ref<Container>>() {
        }).in(Singleton.class);

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

    }
}
