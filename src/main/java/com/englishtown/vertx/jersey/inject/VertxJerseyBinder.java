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
import com.englishtown.vertx.jersey.JerseyServerFactory;
import com.englishtown.vertx.jersey.impl.DefaultApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.impl.DefaultJerseyHandlerConfigurator;
import com.englishtown.vertx.jersey.impl.DefaultJerseyServerFactory;
import com.englishtown.vertx.jersey.inject.impl.VertxResponseWriterProvider;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 *
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
        bind(DefaultJerseyServerFactory.class).to(JerseyServerFactory.class);
        bind(DefaultJerseyHandler.class).to(JerseyHandler.class);
        bind(DefaultJerseyHandlerConfigurator.class).to(JerseyHandlerConfigurator.class);
        bind(VertxResponseWriterProvider.class).to(ContainerResponseWriterProvider.class);

    }

}
