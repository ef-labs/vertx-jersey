package com.englishtown.vertx.jersey.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.hk2.WhenHK2JerseyBinder;
import com.englishtown.vertx.jersey.JerseyVerticle;
import com.englishtown.vertx.promises.WhenHttpClient;
import com.englishtown.vertx.promises.WhenVertx;
import com.englishtown.vertx.promises.hk2.HK2WhenBinder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Base class for jersey integration tests
 */
public abstract class JerseyIntegrationTestBase extends VertxTestBase {

    protected JsonObject config;
    protected ServiceLocator locator;
    protected WhenVertx whenVertx;
    protected HttpClient httpClient;
    protected WhenHttpClient whenHttpClient;
    protected String deploymentID;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        locator = ServiceLocatorFactory.getInstance().create(null);
        ServiceLocatorUtilities.bind(locator, new HK2VertxBinder(vertx), new WhenHK2JerseyBinder(), new HK2WhenBinder());

        config = loadConfig();
        whenVertx = locator.getService(WhenVertx.class);
        whenHttpClient = locator.getService(WhenHttpClient.class);

        HttpClientOptions clientOptions = new HttpClientOptions()
                .setConnectTimeout(1000);

        httpClient = vertx.createHttpClient(clientOptions)
                .exceptionHandler(t -> {
                    t.printStackTrace();
                    fail(t.getMessage());
                });


        CountDownLatch latch = new CountDownLatch(1);

        deployJerseyVerticle()
                .then(id -> {
                    deploymentID = id;
                    return null;
                })
                .otherwise(t -> {
                    t.printStackTrace();
                    fail();
                    return null;
                })
                .ensure(latch::countDown);

        latch.await(2, TimeUnit.SECONDS);

    }

    protected Promise<String> deployJerseyVerticle() {
        return deployJerseyVerticle("java-hk2:" + JerseyVerticle.class.getName());
    }

    protected Promise<String> deployJerseyVerticle(String identifier) {
        DeploymentOptions options = new DeploymentOptions().setConfig(config);
        return whenVertx.deployVerticle(identifier, options);
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        httpClient.close();
        locator.shutdown();
    }

    protected JsonObject loadConfig() {
        return loadConfig("/config.json");
    }

    protected JsonObject loadConfig(String name) {

        try (InputStream is = this.getClass().getResourceAsStream(name)) {
            try (Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A")) {
                return scanner.hasNext() ? new JsonObject(scanner.next()) : new JsonObject();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected Promise<Void> onRejected(Throwable t) {
        t.printStackTrace();
        fail();
        return null;
    }

}
