package com.englishtown.vertx.jersey.features.swagger.internal;

import io.swagger.config.SwaggerConfig;
import io.swagger.jaxrs.config.SwaggerContextService;
import io.swagger.models.Swagger;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

class SwaggerServletConfig implements ServletConfig {

    private ServletContext context;
    private List<String> initParams = new ArrayList<>();

    @Inject
    public SwaggerServletConfig(ServletContext context) {
        this.context = context;

        new SwaggerContextService()
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
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParams);
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
