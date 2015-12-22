package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.examples.resources.TestResource;
import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

public class InjectionIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/test";

    @Test
    public void testGet_OK() throws Exception {

        getWhenHttpClient().requestAbs(HttpMethod.GET, BASE_PATH, new RequestOptions().setPauseResponse(true))
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    return getWhenHttpClient().body(response);
                })
                .then(body -> {
                    String str = body.toString();
                    assertEquals(str, TestResource.RESPONSE_BODY);
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}