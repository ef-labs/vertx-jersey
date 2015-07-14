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

package com.englishtown.vertx.hk2;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.impl.*;
import com.englishtown.vertx.jersey.inject.ContainerResponseWriterProvider;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import com.englishtown.vertx.jersey.inject.impl.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * HK2 Jersey binder
 */
public class HK2JerseyBinder extends AbstractBinder {

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

    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        bind(DefaultApplicationHandlerDelegate.class).to(ApplicationHandlerDelegate.class);
        bind(DefaultJerseyServer.class).to(JerseyServer.class);
        bind(DefaultJerseyHandler.class).to(JerseyHandler.class);
        bind(DefaultJerseyOptions.class).to(JerseyOptions.class);
        bind(VertxResponseWriterProvider.class).to(ContainerResponseWriterProvider.class);
        bind(JsonArrayReader.class).to(new TypeLiteral<MessageBodyReader<JsonArray>>() {
        });
        bind(JsonArrayWriter.class).to(new TypeLiteral<MessageBodyWriter<JsonArray>>() {
        });
        bind(JsonObjectReader.class).to(new TypeLiteral<MessageBodyReader<JsonObject>>() {
        });
        bind(JsonObjectWriter.class).to(new TypeLiteral<MessageBodyWriter<JsonObject>>() {
        });
        bind(WriteStreamBodyWriter.class).to(MessageBodyWriter.class).in(Singleton.class);

        bindFactory(VertxRequestProcessorFactory.class).to(new TypeLiteral<List<VertxRequestProcessor>>() {
        });
        bindFactory(VertxResponseProcessorFactory.class).to(new TypeLiteral<List<VertxResponseProcessor>>() {
        });
        bindFactory(VertxPostResponseProcessorFactory.class).to(new TypeLiteral<List<VertxPostResponseProcessor>>() {
        });

    }

}
