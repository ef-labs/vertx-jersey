package com.englishtown.vertx.jersey.features.jackson.integration;

import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Consumer;

/**
 * Integration tests for Jackson feature
 */
public class JacksonIntegrationTest extends JerseyHK2IntegrationTestBase {

    public static final String BASE_PATH = "http://localhost:8080/test";

    @Test
    public void testJsonObject_Empty() throws Exception {
        runTest(BASE_PATH, Response.Status.NO_CONTENT.getStatusCode(), getRequestOptions(), body -> {
            assertEquals(0, body.length());
        });
    }

    @Test
    public void testJsonObject() throws Exception {

        JsonObject json = new JsonObject()
                .put("b", "c")
                .put("a", 1);

        RequestOptions options = getRequestOptions()
                .setData(json.encode());

        runTest(BASE_PATH, Response.Status.OK.getStatusCode(), options, body -> {
            JsonObject result = new JsonObject(body.toString());
            assertEquals(json, result);
        });

    }

    @Test
    public void testJsonObject_Bad_Request() throws Exception {

        RequestOptions options = getRequestOptions()
                .setData("{ ...");

        runTest(BASE_PATH, Response.Status.BAD_REQUEST.getStatusCode(), options, null);

    }

    @Test
    public void testJsonArray() throws Exception {

        JsonArray json = new JsonArray()
                .add(1)
                .add("a");

        RequestOptions options = getRequestOptions()
                .setData(json.encode());

        runTest(BASE_PATH + "/array", Response.Status.OK.getStatusCode(), options, body -> {
            JsonArray result = new JsonArray(body.toString());
            assertEquals(json, result);
        });

    }

    @Test
    public void testJsonArray_Bad_Request() throws Exception {

        RequestOptions options = getRequestOptions()
                .setData("[1, ...");

        runTest(BASE_PATH + "/array", Response.Status.BAD_REQUEST.getStatusCode(), options, null);

    }

    @Test
    public void testObject() throws Exception {

        JsonObject json = new JsonObject()
                .put("prop1", "c")
                .put("prop2", 1)
                .put("prop_with_camel_casing_to_replace", "a");

        RequestOptions options = getRequestOptions()
                .setData(json.encode());

        runTest(BASE_PATH + "/obj", Response.Status.OK.getStatusCode(), options, body -> {
            JsonObject result = new JsonObject(body.toString());
            assertEquals(json, result);
        });

    }

    private void runTest(String path, int expectedStatus, RequestOptions options, Consumer<Buffer> bodyValidator) throws Exception {

        whenHttpClient.requestAbs(HttpMethod.POST, path, options)
                .then(response -> {
                    assertEquals(expectedStatus, response.statusCode());
                    return whenHttpClient.body(response);
                })
                .then(body -> {
                    if (bodyValidator != null) {
                        bodyValidator.accept(body);
                    }
                    testComplete();
                    return null;
                });

        await();

    }

    private RequestOptions getRequestOptions() {
        return new RequestOptions()
                .setPauseResponse(true)
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

}
