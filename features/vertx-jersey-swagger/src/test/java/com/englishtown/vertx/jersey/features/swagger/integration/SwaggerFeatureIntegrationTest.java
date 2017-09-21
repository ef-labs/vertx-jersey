package com.englishtown.vertx.jersey.features.swagger.integration;

import com.englishtown.vertx.jersey.features.swagger.internal.SwaggerCorsFilter;
import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import static org.junit.Assert.*;

/**
 * Swagger feature integration test
 */
public class SwaggerFeatureIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/rest/";

    @Override
    public void setUp() throws Exception {

        // Need to reset the static initialized flag
        Field field = ApiListingResource.class.getSuperclass().getDeclaredField("initialized");
        field.setAccessible(true);
        field.set(null, false);

        super.setUp();

    }

    @Test
    public void testSwaggerJson() throws Exception {

        runTest("swagger.json", body -> {
            JsonObject json = new JsonObject(body.toString());
            assertNotNull(json);
            assertEquals("2.0", json.getString("swagger"));
        });

    }

    @Test
    public void testSwaggerYaml() throws Exception {

        runTest("swagger.yaml", body -> {
            String yaml = body.toString();
            assertFalse(yaml.isEmpty());
            assertTrue(yaml.contains("swagger: \"2.0\""));
        });

    }

    private void runTest(String additionalPath, Consumer<Buffer> assertMethod) throws Exception {

        RequestOptions options = new RequestOptions()
                .setPauseResponse(true)
                .addHeader(SwaggerCorsFilter.HEADER_ORIGIN, "http://test.org");

        getWhenHttpClient().requestAbs(HttpMethod.GET, BASE_PATH + additionalPath, options)
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    assertEquals("*", response.getHeader(SwaggerCorsFilter.HEADER_ACCESS_CONTROL_ALLOW_ORIGIN));
                    return getWhenHttpClient().body(response);
                })
                .then(body -> {
                    assertMethod.accept(body);
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }}