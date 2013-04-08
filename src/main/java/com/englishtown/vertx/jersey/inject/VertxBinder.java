package com.englishtown.vertx.jersey.inject;

import com.englishtown.vertx.jersey.VertxParam;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Singleton;

/**
 * Binder to register VertxParam injections
 */
public class VertxBinder extends AbstractBinder {
    @Override
    protected void configure() {

        bind(VertxParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);

        bind(VertxParamValueFactoryProvider.InjectionResolver.class).to(new TypeLiteral<InjectionResolver<VertxParam>>
                () {
        }).in(Singleton.class);

    }
}
