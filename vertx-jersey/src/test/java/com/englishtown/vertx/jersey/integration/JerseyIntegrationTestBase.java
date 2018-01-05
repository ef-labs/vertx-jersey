package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.promises.WhenHttpClient;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.test.core.VertxTestBase;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Base integration test class
 */
public abstract class JerseyIntegrationTestBase extends VertxTestBase {

    private final TestServiceLocator locator;
    protected HttpClient httpClient;
    protected String deploymentID;

    protected JerseyIntegrationTestBase(TestServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        init();
    }

    protected void init() throws Exception {

        locator.init(vertx);

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setConnectTimeout(1000);

        httpClient = vertx.createHttpClient(clientOptions);

        CompletableFuture<String> future = new CompletableFuture<>();

        locator.deployJerseyVerticle()
                .then(id -> {
                    future.complete(id);
                    return null;
                })
                .otherwise(t -> {
                    future.completeExceptionally(t);
                    return null;
                });

        deploymentID = future.get(10, TimeUnit.SECONDS);

    }

    @Override
    protected void tearDown() throws Exception {
        httpClient.close();
        locator.tearDown();
        super.tearDown();
    }

    protected Promise<Void> onRejected(Throwable t) {
        t.printStackTrace();
        fail();
        return null;
    }

    protected WhenHttpClient getWhenHttpClient() {
        return locator.getWhenHttpClient();
    }

}
