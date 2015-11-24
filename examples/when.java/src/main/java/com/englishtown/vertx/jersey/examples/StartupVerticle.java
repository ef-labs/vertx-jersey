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

        jerseyServer.createServer()
                .then(server -> {
                    startedResult.complete();
                    return null;
                })
                .otherwise(t -> {
                    startedResult.fail(t);
                    return null;
                });

    }
}
