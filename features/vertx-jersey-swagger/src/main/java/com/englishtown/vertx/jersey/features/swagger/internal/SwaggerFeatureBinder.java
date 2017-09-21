package com.englishtown.vertx.jersey.features.swagger.internal;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class SwaggerFeatureBinder extends AbstractBinder {

    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {
        bind(SwaggerServletContext.class).to(ServletContext.class).in(Singleton.class);
        bind(SwaggerServletConfig.class).to(ServletConfig.class).in(Singleton.class);
    }

}
