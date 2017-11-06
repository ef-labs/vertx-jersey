package com.englishtown.vertx.jersey.features.swagger.internal;

import io.swagger.config.SwaggerConfig;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Swagger;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Configuration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.swagger.jaxrs.config.SwaggerContextService.*;

class SwaggerServletConfig implements ServletConfig {

    private ServletContext context;
    private Map<String, String> initParams = new HashMap<>();

    public static final String PROPERTY_PREFIX = "swagger.";

    @Inject
    public SwaggerServletConfig(ServletContext context, Configuration config) {
        this.context = context;

        // Copy swagger properties to init params
        config.getProperties()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().startsWith(PROPERTY_PREFIX))
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> initParams.put(entry.getKey(), entry.getValue().toString()));

        Function<String, String> getValue = name -> {
            Object val = config.getProperty(name);
            return val == null ? null : val.toString();
        };

        new SwaggerContextService()
                .withConfigId(getValue.apply(CONFIG_ID_KEY))
                .withScannerId(getValue.apply(SCANNER_ID_KEY))
                .withContextId(getValue.apply(CONTEXT_ID_KEY))
                .withBasePath(context.getContextPath())
                .withSwaggerConfig(new DefaultSwaggerConfig(context))
                .withServletConfig(this)
                .initConfig()
                .initScanner();

    }

    @Override
    public String getServletName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return initParams.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParams.keySet());
    }

    private static class DefaultSwaggerConfig implements SwaggerConfig {

        private ServletContext context;

        public DefaultSwaggerConfig(ServletContext context) {
            this.context = context;
        }

        @Override
        public Swagger configure(Swagger swagger) {
            String basePath = context.getContextPath();
            if (basePath.endsWith("/")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            swagger.setBasePath(basePath);
            return swagger;
        }

        @Override
        public String getFilterClass() {
            return null;
        }

    }
}
