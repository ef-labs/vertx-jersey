package com.englishtown.vertx.jersey.integration;

import com.englishtown.vertx.jersey.inject.VertxJerseyBinder;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Provider;

/**
 *
 */
public class IntegrationBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        install(new VertxJerseyBinder());

        bind(ReqProcessor1.class).to(VertxRequestProcessor.class).ranked(10);
        bind(ReqProcessor2.class).to(VertxRequestProcessor.class).ranked(100);
        bind(InjectionBinderProvider.class).to(new TypeLiteral<Provider<Binder>>() {
        });

    }
}
