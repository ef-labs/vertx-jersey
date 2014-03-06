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
import com.englishtown.vertx.jersey.JerseyConfigurator;
import com.englishtown.vertx.jersey.inject.InternalVertxJerseyBinder;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import javax.inject.Provider;
import java.net.URI;
import java.util.List;

/**
 * Default {@link com.englishtown.vertx.jersey.JerseyConfigurator} implementation
 */
public class DefaultJerseyConfigurator implements JerseyConfigurator {

    final static String CONFIG_HOST = "host";
    final static String CONFIG_PORT = "port";
    final static String CONFIG_SSL = "ssl";
    final static String CONFIG_KEY_STORE_PASSWORD = "key_store_password";
    final static String CONFIG_KEY_STORE_PATH = "key_store_path";
    final static String CONFIG_RECEIVE_BUFFER_SIZE = "receive_buffer_size";
    final static String CONFIG_BACKLOG_SIZE = "backlog_size";

    public static final String CONFIG_BASE_PATH = "base_path";
    public static final String CONFIG_MAX_BODY_SIZE = "max_body_size";
    public static final String CONFIG_RESOURCES = "resources";
    public static final String CONFIG_FEATURES = "features";
    public static final String CONFIG_BINDERS = "binders";
    public static final int DEFAULT_MAX_BODY_SIZE = 1024 * 1000; // Default max body size to 1MB

    private final List<Provider<Binder>> binderProviders;

    private Vertx vertx;
    private Container container;
    private JsonObject config;
    private Handler<ResourceConfig> resourceConfigHandler;

    @Inject
    public DefaultJerseyConfigurator(List<Provider<Binder>> binderProviders) {
        this.binderProviders = binderProviders;
    }

    @Override
    public void init(JsonObject config, Vertx vertx, Container container) {
        this.vertx = vertx;
        this.container = container;

        if (config == null) {
            throw new IllegalStateException("The provided configuration was null");
        }
        this.config = config;
    }

    /**
     * Returns the current vertx instance
     *
     * @return the {@link org.vertx.java.core.Vertx} instance
     */
    @Override
    public Vertx getVertx() {
        return vertx;
    }

    /**
     * Returns the current container instance
     *
     * @return the {@link org.vertx.java.platform.Container} instance
     */
    @Override
    public Container getContainer() {
        return container;
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
     * The key store password when using ssl
     *
     * @return the key store password
     */
    @Override
    public String getKeyStorePassword() {
        return config.getString(CONFIG_KEY_STORE_PASSWORD, "wibble");
    }

    /**
     * The key store path when using ssl
     *
     * @return the key store path
     */
    @Override
    public String getKeyStorePath() {
        return config.getString(CONFIG_KEY_STORE_PATH, "server-keystore.jks");
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

        // Always register the InternalVertxJerseyBinder
        rc.register(new InternalVertxJerseyBinder(vertx, container));

        // Register configured binders
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

        // Register any additional binders
        if (binderProviders != null) {
            for (Provider<Binder> provider : binderProviders) {
                rc.register(provider.get());
            }
        }

        if (resourceConfigHandler != null) {
            resourceConfigHandler.handle(rc);
        }

        return rc;

    }

    private void checkState() {
        if (config == null) {
            throw new IllegalStateException("The configurator has not been initialized.");
        }
    }

}
