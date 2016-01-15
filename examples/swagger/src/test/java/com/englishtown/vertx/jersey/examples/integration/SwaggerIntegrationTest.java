package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class SwaggerIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/";

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Need to reset the static initialized flag
        Field field = ApiListingResource.class.getDeclaredField("initialized");
        field.setAccessible(true);
        field.set(null, false);

    }

    @Test
    public void testSwaggerJson() throws Exception {

        runTest("swagger.json", body -> {
            JsonObject json = new JsonObject(body.toString());

            JsonObject paths = json.getJsonObject("paths");
            assertNotNull(paths);
            JsonObject path = paths.getJsonObject("/swagger-test");
            assertNotNull(path);
            assertTrue(path.containsKey("get"));
            assertTrue(path.containsKey("post"));

            JsonObject definitions = json.getJsonObject("definitions");
            assertNotNull(definitions);
            assertTrue(definitions.containsKey("MyObject"));

        });

    }

    @Test
    public void testSwaggerYaml() throws Exception {

        runTest("swagger.yaml", body -> {
            String yaml = body.toString();
            assertFalse(yaml.isEmpty());
        });

    }

    private void runTest(String additionalPath, Consumer<Buffer> assertMethod) throws Exception {

        getWhenHttpClient().requestAbs(HttpMethod.GET, BASE_PATH + additionalPath, new RequestOptions().setPauseResponse(true))
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    return getWhenHttpClient().body(response);
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