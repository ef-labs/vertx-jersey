package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.integration.JerseyGuiceIntegrationTestBase;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

public class GuiceIntegrationTest extends JerseyGuiceIntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/guice";

    @Test
    public void testGet() throws Exception {

        whenHttpClient.requestAbs(HttpMethod.GET, BASE_PATH)
                .then(response -> {
                    assertEquals(200, response.statusCode());

                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}