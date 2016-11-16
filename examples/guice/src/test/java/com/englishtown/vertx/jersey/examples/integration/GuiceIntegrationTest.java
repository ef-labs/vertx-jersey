package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.examples.guice.inject.GuiceExceptionMapper;
import com.englishtown.vertx.jersey.integration.JerseyGuiceIntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

public class GuiceIntegrationTest extends JerseyGuiceIntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/guice";

    @Test
    public void testGet() throws Exception {

        getWhenHttpClient().requestAbs(HttpMethod.GET, BASE_PATH)
                .then(response -> {
                    assertEquals(200, response.statusCode());

                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

    @Test
    public void testExceptionMapper() throws Exception {

        RequestOptions options = new RequestOptions().setPauseResponse(true);

        getWhenHttpClient().requestAbs(HttpMethod.GET, BASE_PATH + "/exception", options)
                .then(response -> {
                    assertEquals(GuiceExceptionMapper.STATUS_IM_A_TEAPOT.getStatusCode(), response.statusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeader(HttpHeaders.CONTENT_TYPE));
                    return getWhenHttpClient().body(response);
                })
                .then(body -> {
                    JsonObject json = body.toJsonObject();
                    assertTrue(json.containsKey("msg"));
                    assertTrue(json.containsKey("type"));

                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}