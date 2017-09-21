package com.englishtown.vertx.jersey.features.swagger.internal;

import com.englishtown.vertx.jersey.features.swagger.SwaggerFeature;
import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.spi.AutoDiscoverable;

import javax.annotation.Priority;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
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
