package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.examples.PoweredByResponseFilter;
import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

public class FilterIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/test";

    @Test
    public void testGet() throws Exception {

        whenHttpClient.requestAbs(HttpMethod.GET, BASE_PATH)
                .then(response -> {
                    assertEquals(200, response.statusCode());

                    String header = response.getHeader(PoweredByResponseFilter.HEADER_X_POWERED_BY);
                    assertEquals(PoweredByResponseFilter.HEADER_X_POWERED_BY_VALUE, header);

                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}