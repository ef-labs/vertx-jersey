package com.englishtown.vertx.jersey.features.jackson;

import com.englishtown.vertx.jersey.features.jackson.internal.JsonConfigObjectMapperConfigurator;
import com.englishtown.vertx.jersey.features.jackson.internal.ObjectMapperProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Vert.x Jackson Feature
 */
public class JacksonFeature implements Feature {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {

        if (!context.getConfiguration().isRegistered(ObjectMapperConfigurator.class)) {
            context.register(new Binder());
            context.register(ObjectMapperProvider.class);
        }

        return true;
    }

    private static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(JsonConfigObjectMapperConfigurator.class).to(ObjectMapperConfigurator.class).in(Singleton.class);
        }
    }

}
