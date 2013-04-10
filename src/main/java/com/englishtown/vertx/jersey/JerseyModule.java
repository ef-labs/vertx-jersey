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

package com.englishtown.vertx.jersey;

import com.englishtown.vertx.jersey.inject.VertxBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidResult;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.net.URI;

/**
 * The Vertx Module to enable Jersey to handle JAX-RS resources
 */
public class JerseyModule extends BusModBase {

    private final String CONFIG_HOST = "host";
    private final String CONFIG_PORT = "port";
    private final String CONFIG_BASE_PATH = "base_path";
    private final String CONFIG_RESOURCES = "resources";
    private final String CONFIG_FEATURES = "features";
    private final String CONFIG_BINDERS = "binders";

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final VoidResult startedResult) throws Exception {
        this.start();

        String host = getOptionalStringConfig(CONFIG_HOST, "0.0.0.0");
        int port = getOptionalIntConfig(CONFIG_PORT, 80);
        String basePath = getOptionalStringConfig(CONFIG_BASE_PATH, "/");
        ResourceConfig rc = getResourceConfig(config);

        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        RouteMatcher rm = new RouteMatcher();
        rm.all(basePath + ".*", new JerseyHandler(vertx, container, URI.create(basePath), rc));

        vertx.createHttpServer().requestHandler(rm).listen(port, host, new Handler<HttpServer>() {
            @Override
            public void handle(HttpServer event) {
                startedResult.setResult();
            }
        });

        container.getLogger().info("Http server listening for http://" + host + ":" + port + basePath);

    }

    ResourceConfig getResourceConfig(JsonObject config) {

        JsonArray resources = config.getArray(CONFIG_RESOURCES, null);

        if (resources == null || resources.size() == 0) {
            throw new RuntimeException("At lease one resource package name must be specified in the config " +
                    CONFIG_RESOURCES);
        }

        String[] resourceArr = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            resourceArr[i] = String.valueOf(resources.get(i));
        }

        ResourceConfig rc = new ResourceConfig();
        rc.packages(resourceArr);
        rc.registerInstances(new VertxBinder());

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
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return rc;

    }

}
