package com.englishtown.vertx.jersey.inject;

import com.englishtown.vertx.jersey.DefaultJerseyHandler;
import com.englishtown.vertx.jersey.JerseyHandler;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;
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
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        install(new VertxParamBinder());

        bindFactory(VertxResponseProcessorFactory.class).to(new TypeLiteral<List<VertxResponseProcessor>>() {
        });
        bindFactory(VertxRequestProcessorFactory.class).to(new TypeLiteral<List<VertxRequestProcessor>>() {
        });

        bind(DefaultJerseyHandler.class).to(JerseyHandler.class);
        bind(VertxResponseWriterProvider.class).to(ContainerResponseWriterProvider.class);

    }
}
