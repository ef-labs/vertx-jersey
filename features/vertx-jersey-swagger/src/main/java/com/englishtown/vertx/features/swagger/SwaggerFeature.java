package com.englishtown.vertx.features.swagger;

import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Jersey Swagger Feature
 */
public class SwaggerFeature implements Feature {

    private final SwaggerFeatureConfig config;

    @Inject
    public SwaggerFeature(SwaggerFeatureConfig config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {

        if (config != null && !config.enabled()) {
            return false;
        }

        if (!context.getConfiguration().isRegistered(ApiListingResource.class)) {
            context.register(ApiListingResource.class);
            context.register(SwaggerSerializers.class);
        }

        if (ScannerFactory.getScanner() == null) {
            ScannerFactory.setScanner(new DefaultJaxrsScanner());
        }

        return true;
    }
}
