package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.promises.WhenHttpClient;
import com.englishtown.vertx.promises.WhenVertx;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base integration test class
 */
public abstract class JerseyIntegrationTestBase extends VertxTestBase {

    protected JsonObject config;
    protected WhenVertx whenVertx;
    protected HttpClient httpClient;
    protected WhenHttpClient whenHttpClient;
    protected String deploymentID;

    protected abstract <T> T getService(Class<T> clazz);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        init();
    }

    protected void init() throws Exception {

        config = loadConfig();
        whenVertx = getService(WhenVertx.class);
        whenHttpClient = getService(WhenHttpClient.class);

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setConnectTimeout(1000);

        httpClient = vertx.createHttpClient(clientOptions);

        CountDownLatch latch = new CountDownLatch(1);

        deployJerseyVerticle()
                .then(id -> {
                    deploymentID = id;
                    return null;
                })
                .otherwise(t -> {
                    try {
                        t.printStackTrace();
                        fail(t.getMessage());
                    } catch (Throwable t2) {
                        t2.printStackTrace();
                        fail(t2.getMessage());
                    }
                    return null;
                })
                .ensure(latch::countDown);

        latch.await(2, TimeUnit.SECONDS);

    }

    protected abstract Promise<String> deployJerseyVerticle();

    protected Promise<String> deployJerseyVerticle(String identifier) {
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        return whenVertx.deployVerticle(identifier, options);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        httpClient.close();
    }

    protected JsonObject loadConfig() {
        return ConfigUtils.loadConfig();
    }

    protected Promise<Void> onRejected(Throwable t) {
        t.printStackTrace();
        fail();
        return null;
    }

}
