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
import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.DefaultApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.impl.DefaultJerseyConfigurator;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServer;
import com.englishtown.vertx.jersey.inject.impl.VertxResponseWriterProvider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class VertxJerseyBinder extends AbstractBinder {

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

    static class VertxPostResponseProcessorFactory implements Factory<List<VertxPostResponseProcessor>> {

        private final List<VertxPostResponseProcessor> processors = new ArrayList<>();

        @Inject
        public VertxPostResponseProcessorFactory(IterableProvider<VertxPostResponseProcessor> providers) {
            for (VertxPostResponseProcessor processor : providers) {
                processors.add(processor);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<VertxPostResponseProcessor> provide() {
            return processors;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose(List<VertxPostResponseProcessor> instance) {
        }
    }

    static class BinderProviderFactory implements Factory<List<Provider<Binder>>> {

        private final List<Provider<Binder>> binders = new ArrayList<>();

        @Inject
        public BinderProviderFactory(IterableProvider<Provider<Binder>> providers) {
            for (Provider<Binder> binderProvider : providers) {
                binders.add(binderProvider);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Provider<Binder>> provide() {
            return binders;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose(List<Provider<Binder>> instance) {
        }
    }

    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        bind(DefaultApplicationHandlerDelegate.class).to(ApplicationHandlerDelegate.class);
        bind(DefaultJerseyServer.class).to(JerseyServer.class);
        bind(DefaultJerseyHandler.class).to(JerseyHandler.class);
        bind(DefaultJerseyConfigurator.class).to(JerseyConfigurator.class);
        bind(VertxResponseWriterProvider.class).to(ContainerResponseWriterProvider.class);

        bindFactory(VertxRequestProcessorFactory.class).to(new TypeLiteral<List<VertxRequestProcessor>>() {
        });
        bindFactory(VertxResponseProcessorFactory.class).to(new TypeLiteral<List<VertxResponseProcessor>>() {
        });
        bindFactory(VertxPostResponseProcessorFactory.class).to(new TypeLiteral<List<VertxPostResponseProcessor>>() {
        });
        bindFactory(BinderProviderFactory.class).to(new TypeLiteral<List<Provider<Binder>>>() {
        });

    }

}
