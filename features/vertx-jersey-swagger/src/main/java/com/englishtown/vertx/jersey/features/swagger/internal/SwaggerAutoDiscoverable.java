package com.englishtown.vertx.jersey.features.swagger.internal;

import com.englishtown.vertx.jersey.features.swagger.SwaggerFeature;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.ws.rs.core.FeatureContext;

/**
 * Swagger auto discoverable feature
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public class SwaggerAutoDiscoverable implements AutoDiscoverable {

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(FeatureContext context) {

        if (!context.getConfiguration().isRegistered(SwaggerFeature.class)) {
            context.register(SwaggerFeature.class);
        }

    }

}
