package com.englishtown.vertx.jersey.features.swagger;

import com.englishtown.vertx.jersey.features.swagger.internal.SwaggerCorsFilter;
import com.englishtown.vertx.jersey.features.swagger.internal.SwaggerFeatureBinder;
import io.swagger.config.ScannerFactory;
import io.swagger.jaxrs.config.DefaultJaxrsScanner;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.glassfish.jersey.internal.util.PropertiesHelper;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

/**
 * Jersey Swagger Feature
 */
public class SwaggerFeature implements Feature {

    public static final String PROPERTY_DISABLE = "vertx.jersey.swagger.disable";
    public static final String PROPERTY_CORS_DISABLE = "vertx.jersey.swagger.cors.disable";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configure(FeatureContext context) {

        Configuration config = context.getConfiguration();

        if (PropertiesHelper.isProperty(config.getProperties(), PROPERTY_DISABLE)) {
            return false;
        }

        if (!PropertiesHelper.isProperty(config.getProperties(), PROPERTY_CORS_DISABLE)) {
            context.register(SwaggerCorsFilter.class);
        }

        if (!config.isRegistered(ApiListingResource.class)) {
            context.register(ApiListingResource.class);
            context.register(SwaggerSerializers.class);
        }

        if (ScannerFactory.getScanner() == null) {
            ScannerFactory.setScanner(new DefaultJaxrsScanner());
        }

        if (!config.isRegistered(ServletContext.class)) {
            context.register(new SwaggerFeatureBinder());
        }

        return true;
    }

}
