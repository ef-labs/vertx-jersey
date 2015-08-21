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
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.inject.InternalVertxJerseyBinder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.jvnet.hk2.annotations.Optional;

import javax.inject.Inject;
import java.net.URI;

/**
 * Default {@link com.englishtown.vertx.jersey.JerseyOptions} implementation
 */
public class DefaultJerseyOptions implements JerseyOptions {

    final static String CONFIG_HOST = "host";
    final static String CONFIG_PORT = "port";
    final static String CONFIG_SSL = "ssl";
    final static String CONFIG_JKS_OPTIONS = "jks_options";
    final static String CONFIG_RECEIVE_BUFFER_SIZE = "receive_buffer_size";
    final static String CONFIG_BACKLOG_SIZE = "backlog_size";
    final static String CONFIG_RESOURCE_CONFIG = "resource_config";
    final static String CONFIG_COMPRESSION_SUPPORTED = "compression_supported";

    public static final String CONFIG_BASE_PATH = "base_path";
    public static final String CONFIG_MAX_BODY_SIZE = "max_body_size";
    public static final String CONFIG_RESOURCES = "resources";
    public static final String CONFIG_FEATURES = "features";
    public static final String CONFIG_BINDERS = "binders";
    public static final int DEFAULT_MAX_BODY_SIZE = 1024 * 1000; // Default max body size to 1MB

    private final ServiceLocator locator;

    private Vertx vertx;
    private JsonObject config;

    /**
     * Injection constructor
     *
     * @param locator an optional ServiceLocator instance to be the Jersey parent locator
     */
    @Inject
    public DefaultJerseyOptions(@Optional ServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public void init(JsonObject config, Vertx vertx) {
        this.vertx = vertx;

        if (config == null) {
            throw new IllegalStateException("The provided configuration was null");
        }
        this.config = config;
    }

    /**
     * Returns the current vertx instance
     *
     * @return the {@link io.vertx.core.Vertx} instance
     */
    @Override
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * The http web server host
     *
     * @return the http web server host to listen to
     */
    @Override
    public String getHost() {
        checkState();
        return config.getString(CONFIG_HOST, "0.0.0.0");
    }

    /**
     * The http web server port
     *
     * @return the http web server port to listen to
     */
    @Override
    public int getPort() {
        checkState();
        return config.getInteger(CONFIG_PORT, 80);
    }

    /**
     * Whether the web server should be https.
     *
     * @return whether the web server should be https.
     */
    @Override
    public boolean getSSL() {
        return config.getBoolean(CONFIG_SSL, false);
    }

    /**
     * Vert.x http server key store options
     *
     * @return Java key store options
     */
    @Override
    public JksOptions getKeyStoreOptions() {
        JsonObject json = config.getJsonObject(CONFIG_JKS_OPTIONS);
        return json == null ? null : new JksOptions(json);
    }

    /**
     * The TCP receive buffer size for connections in bytes
     *
     * @return buffer size in bytes
     */
    @Override
    public Integer getReceiveBufferSize() {
        checkState();
        return config.getInteger(CONFIG_RECEIVE_BUFFER_SIZE);
    }

    /**
     * The accept backlog
     *
     * @return the accept backlog
     */
    @Override
    public int getAcceptBacklog() {
        checkState();
        return config.getInteger(CONFIG_BACKLOG_SIZE, 10000);
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
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }
        return URI.create(basePath);
    }

    /**
     * Returns the Jersey {@link org.glassfish.jersey.server.ApplicationHandler} instance
     *
     * @return the application handler instance
     */
    @Override
    public ApplicationHandlerDelegate getApplicationHandler() {
        ApplicationHandler handler = new ApplicationHandler(getResourceConfig(), null, locator);
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
        return config.getInteger(CONFIG_MAX_BODY_SIZE, DEFAULT_MAX_BODY_SIZE);
    }

    /**
     * Gets whether the server supports compression (defaults to false)
     *
     * @return whether compression is supported
     */
    @Override
    public boolean getCompressionSupported() {
        checkState();
        return config.getBoolean(CONFIG_COMPRESSION_SUPPORTED, false);
    }

    protected ResourceConfig getResourceConfig() {
        checkState();
        JsonArray resources = config.getJsonArray(CONFIG_RESOURCES, null);

        if (resources == null || resources.size() == 0) {
            throw new RuntimeException("At least one resource package name must be specified in the config " +
                    CONFIG_RESOURCES);
        }

        String[] resourceArr = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            resourceArr[i] = resources.getString(i);
        }

        ResourceConfig rc = new ResourceConfig();
        rc.packages(resourceArr);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        JsonArray features = config.getJsonArray(CONFIG_FEATURES, null);
        if (features != null && features.size() > 0) {
            for (int i = 0; i < features.size(); i++) {
                try {
                    Class<?> clazz = cl.loadClass(features.getString(i));
                    rc.register(clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        // Always register the InternalVertxJerseyBinder
        rc.register(new InternalVertxJerseyBinder(vertx));

        // Register configured binders
        JsonArray binders = config.getJsonArray(CONFIG_BINDERS, null);
        if (binders != null && binders.size() > 0) {
            for (int i = 0; i < binders.size(); i++) {
                try {
                    Class<?> clazz = cl.loadClass(binders.getString(i));
                    rc.register(clazz.newInstance());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        final JsonObject resourceConfigAdditions = config.getJsonObject(CONFIG_RESOURCE_CONFIG);
        if (resourceConfigAdditions != null) {
            for (final String fieldName : resourceConfigAdditions.fieldNames()) {
                rc.property(fieldName, resourceConfigAdditions.getValue(fieldName));
            }
        }

        return rc;
    }

    private void checkState() {
        if (config == null) {
            throw new IllegalStateException("The jersey options have not been initialized.");
        }
    }

}
