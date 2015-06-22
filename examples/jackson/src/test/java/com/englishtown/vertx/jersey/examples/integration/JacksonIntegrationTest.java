package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.function.Consumer;

public class JacksonIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/json";

    @Test
    public void testGet() throws Exception {

        runTest("", body -> {
            JsonObject json = new JsonObject(body.toString());
            assertEquals("Andy", json.getString("name"));
        });

    }

    @Test
    public void testGetAsync() throws Exception {

        runTest("/async", body -> {
            JsonObject json = new JsonObject(body.toString());
            assertEquals("Andy", json.getString("name"));
        });

    }

    @Test
    public void testGetJsonp() throws Exception {

        runTest("/jsonp?cb=myCallback", body -> {
            assertEquals("myCallback({\"name\":\"Andy\"})", body.toString());
        });

    }

    @Test
    public void testPost() throws Exception {

        RequestOptions options = new RequestOptions()
                .setPauseResponse(true)
                .setSetupHandler(request -> {
                    request.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                    return null;
                })
                .setData(new JsonObject().put("name", "Andy").encode());

        whenHttpClient.requestAbs(HttpMethod.POST, BASE_PATH, options)
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    return whenHttpClient.body(response);
                })
                .then(body -> {
                    JsonObject json = new JsonObject(body.toString());
                    assertEquals("Andy", json.getString("name"));
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

    private void runTest(String additionalPath, Consumer<Buffer> assertMethod) throws Exception {

        whenHttpClient.requestAbs(HttpMethod.GET, BASE_PATH + additionalPath, new RequestOptions().setPauseResponse(true))
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    return whenHttpClient.body(response);
                })
                .then(body -> {
                    assertMethod.accept(body);
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}