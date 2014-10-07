package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.promises.WhenJerseyServer;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import javax.inject.Inject;

/**
 * {@link com.englishtown.vertx.jersey.promises.WhenJerseyServer} example
 */
public class StartupVerticle extends Verticle {

    private final WhenJerseyServer jerseyServer;

    @Inject
    public StartupVerticle(WhenJerseyServer jerseyServer) {
        this.jerseyServer = jerseyServer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Future<Void> startedResult) {

        JsonObject jerseyConfig = container.config().getObject("jersey");

        jerseyServer.createServer(jerseyConfig)
                .then(
                        server -> {
                            super.start(startedResult);
                            return null;
                        })
                .otherwise(t -> {
                    startedResult.setFailure(t);
                    return null;
                });

    }
}
