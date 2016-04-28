package com.englishtown.vertx.jersey.features.jackson;

import com.englishtown.vertx.jersey.features.jackson.internal.JsonConfigObjectMapperConfigurator;
import com.englishtown.vertx.jersey.features.jackson.internal.ObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Vert.x Jackson Feature
 */
public class JacksonFeature implements Feature {

    public static final String PROPERTY_DISABLE = "vertx.jersey.jackson.disable";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {

        Configuration config = context.getConfiguration();
        boolean disabled = PropertiesHelper.isProperty(config.getProperties(), PROPERTY_DISABLE);

        if (disabled) {
            return false;
        }

        if (!config.isRegistered(ObjectMapperConfigurator.class)) {
            context.register(new Binder());
            context.register(ObjectMapperProvider.class);
        }

        return true;
    }

    public static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(new ObjectMapper()).to(ObjectMapper.class).ranked(-10);
            bind(JsonConfigObjectMapperConfigurator.class).to(ObjectMapperConfigurator.class).in(Singleton.class);
        }

    }

}
