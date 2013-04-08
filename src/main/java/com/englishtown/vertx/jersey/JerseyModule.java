package com.englishtown.vertx.jersey;

import com.englishtown.vertx.jersey.inject.VertxBinder;
import org.glassfish.jersey.server.ApplicationHandler;
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

        JsonArray resources = config.getArray(CONFIG_RESOURCES);

        if (resources == null || resources.size() == 0) {
            throw new RuntimeException("At lease one package name must be specified in the config " +
                    CONFIG_RESOURCES);
        }

        String[] resourceArr = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            resourceArr[i] = String.valueOf(resources.get(i));
        }

        ResourceConfig rc = new ResourceConfig();
        rc.packages(resourceArr);
        rc.registerInstances(new VertxBinder());

        return rc;

    }

}
