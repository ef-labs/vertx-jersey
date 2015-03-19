package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

public class SecurityContextIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "/test/secure?role=";

    @Test
    public void testGet_OK() throws Exception {
        runTest("1", 200);
    }

    @Test
    public void testGet_Forbidden() throws Exception {
        runTest("3", 403);
    }

    private void runTest(String role, int status) {

        whenHttpClient.request(HttpMethod.GET, 8080, "localhost", BASE_PATH + role)
                .then(response -> {
                    assertEquals(status, response.statusCode());
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}