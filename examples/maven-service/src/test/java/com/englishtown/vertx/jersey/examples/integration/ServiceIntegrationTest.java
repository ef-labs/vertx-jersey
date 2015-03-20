package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.integration.ConfigUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.io.InputStream;
import java.util.Properties;

public class ServiceIntegrationTest extends VertxTestBase {

    @Test
    public void testRunService() throws Exception {

        JsonObject config = ConfigUtils.loadConfig().getJsonObject("jersey");
        Properties props = new Properties();

        try (InputStream is = this.getClass().getResourceAsStream("/service.properties")) {
            props.load(is);
        }

        String name = props.getProperty("service.name");
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        vertx.deployVerticle(name, options, result -> {
            if (result.failed()) {
                fail(result.cause().toString());
            } else {
                runHttpTest();
            }
        });

        await();

    }

    private void runHttpTest() {

        vertx.createHttpClient()
                .requestAbs(HttpMethod.GET, "http://localhost:8080/service", response -> {
                    assertEquals(200, response.statusCode());
                    testComplete();
                })
                .end();


    }

}