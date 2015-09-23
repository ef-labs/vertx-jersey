package com.englishtown.vertx.jersey.features.jackson.internal;

import com.englishtown.vertx.jersey.features.jackson.JacksonFeature;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.ws.rs.core.FeatureContext;

/**
 * Allows {@link JacksonFeature} to be auto discoverable by Jersey
 */
public class JacksonAutoDiscoverable implements AutoDiscoverable {

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(FeatureContext context) {
        if (!context.getConfiguration().isRegistered(JacksonFeature.class)) {
            context.register(JacksonFeature.class);
        }
    }

}
