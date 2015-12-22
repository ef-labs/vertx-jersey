package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.jersey.examples.StartupVerticle;
import com.englishtown.vertx.jersey.integration.HK2TestServiceLocator;
import com.englishtown.vertx.jersey.integration.JerseyIntegrationTestBase;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

public class WhenJavaIntegrationTest extends JerseyIntegrationTestBase {

    public WhenJavaIntegrationTest() {
        super(new HK2TestServiceLocator() {
            @Override
            public Promise<String> deployJerseyVerticle() {
                return deployJerseyVerticle("java-hk2:" + StartupVerticle.class.getName());
            }
        });
    }

    @Test
    public void testGet() throws Exception {

        getWhenHttpClient().requestAbs(HttpMethod.GET, "http://localhost:8080/when")
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

}