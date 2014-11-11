package com.englishtown.vertx.jersey.examples;

import com.englishtown.vertx.jersey.promises.WhenJerseyServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.inject.Inject;

/**
 * {@link com.englishtown.vertx.jersey.promises.WhenJerseyServer} example
 */
public class StartupVerticle extends AbstractVerticle {

    private final WhenJerseyServer jerseyServer;

    @Inject
    public StartupVerticle(WhenJerseyServer jerseyServer) {
        this.jerseyServer = jerseyServer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Future<Void> startedResult) throws Exception {

        JsonObject jerseyConfig = vertx.context().config().getJsonObject("jersey");

        jerseyServer.createServer(jerseyConfig)
                .then(
                        server -> {
                            //TODO Migration: Handle exception correctly
                            try {
                                super.start(startedResult);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        })
                .otherwise(t -> {
                    startedResult.fail(t);
                    return null;
                });

    }
}
