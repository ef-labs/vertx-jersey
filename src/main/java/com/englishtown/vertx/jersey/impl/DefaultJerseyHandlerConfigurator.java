/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey.impl;

import com.englishtown.vertx.jersey.ApplicationHandlerDelegate;
import com.englishtown.vertx.jersey.JerseyHandlerConfigurator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;

/**
 * Default {@link JerseyHandlerConfigurator} implementation
 */
public class DefaultJerseyHandlerConfigurator implements JerseyHandlerConfigurator {

    public static final String CONFIG_BASE_PATH = "base_path";
    public static final String CONFIG_MAX_BODY_SIZE = "max_body_size";
    public static final String CONFIG_RESOURCES = "resources";
    public static final String CONFIG_FEATURES = "features";
    public static final String CONFIG_BINDERS = "binders";
    public static final int DEFAULT_MAX_BODY_SIZE = 1024 * 1000; // Default max body size to 1MB

    private JsonObject config;
    private Logger logger;

    @Override
    public void init(JsonObject config, Logger logger) {
        if (config == null) {
            throw new IllegalStateException("The provided configuration was null");
        }
        this.config = config;
        this.logger = logger;
    }

    /**
     * Returns the base URI used by Jersey
     *
     * @return base URI
     */
    @Override
    public URI getBaseUri() {
        checkState();
        String basePath = config.getString(CONFIG_BASE_PATH, "/");

        // TODO: Does basePath need the trailing "/"?
//        if (!basePath.endsWith("/")) {
//            basePath += "/";
//        }

        return URI.create(basePath);
    }

    /**
     * Returns the Jersey {@link org.glassfish.jersey.server.ApplicationHandler} instance
     *
     * @return the application handler instance
     */
    @Override
    public ApplicationHandlerDelegate getApplicationHandler() {
        setFactory();
        ApplicationHandler handler = new ApplicationHandler(getResourceConfig());
        return new DefaultApplicationHandlerDelegate(handler);
    }

    /**
     * The max body size in bytes when reading the vert.x input stream
     *
     * @return the max body size bytes
     */
    @Override
    public int getMaxBodySize() {
        checkState();
        return config.getNumber(CONFIG_MAX_BODY_SIZE, DEFAULT_MAX_BODY_SIZE).intValue();
    }

    protected ResourceConfig getResourceConfig() {
        checkState();
        JsonArray resources = config.getArray(CONFIG_RESOURCES, null);

        if (resources == null || resources.size() == 0) {
            throw new RuntimeException("At least one resource package name must be specified in the config " +
                    CONFIG_RESOURCES);
        }

        String[] resourceArr = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            resourceArr[i] = String.valueOf(resources.get(i));
        }

        ResourceConfig rc = new ResourceConfig();
        rc.packages(resourceArr);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        JsonArray features = config.getArray(CONFIG_FEATURES, null);
        if (features != null && features.size() > 0) {
            for (int i = 0; i < features.size(); i++) {
                try {
                    Class<?> clazz = cl.loadClass(String.valueOf(features.get(i)));
                    rc.register(clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        JsonArray binders = config.getArray(CONFIG_BINDERS, null);
        if (binders != null && binders.size() > 0) {
            for (int i = 0; i < binders.size(); i++) {
                try {
                    Class<?> clazz = cl.loadClass(String.valueOf(binders.get(i)));
                    rc.register(clazz.newInstance());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return rc;

    }

    private void checkState() {
        if (config == null || logger == null) {
            throw new IllegalStateException("The configurator has not been initialized.");
        }
    }

    private void setFactory() {
        checkState();

        // Jersey stores the ServiceLocatorFactory in a final static field
        // This needs to be reset before being used so each verticle has its own isolated service locator

        try {
            Field field = Injections.class.getDeclaredField("factory");
            field.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, ServiceLocatorFactory.getInstance());

        } catch (NoSuchFieldException e) {
            logger.error("NoSuchFieldException while setting the service locator factory", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException while setting the service locator factory", e);
            throw new RuntimeException(e);
        }

    }

}
