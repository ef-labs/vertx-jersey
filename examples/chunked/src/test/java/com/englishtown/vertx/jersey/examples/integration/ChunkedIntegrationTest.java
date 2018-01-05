package com.englishtown.vertx.jersey.examples.integration;

import com.englishtown.vertx.jersey.integration.JerseyHK2IntegrationTestBase;
import com.englishtown.vertx.promises.RequestOptions;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

public class ChunkedIntegrationTest extends JerseyHK2IntegrationTestBase {

    private String BASE_PATH = "http://localhost:8080/chunked";

    private void runTest(String path) {

        RequestOptions options = new RequestOptions()
                .setPauseResponse(true);

        getWhenHttpClient().requestAbs(HttpMethod.GET, path, options)
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    assertEquals("chunked", response.headers().get(HttpHeaderNames.TRANSFER_ENCODING));
                    return getWhenHttpClient().body(response);
                })
                .then(body -> {
                    String text = body.toString();
                    assertTrue(text.startsWith("aaaaa"));
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();

    }

    @Test
    public void testGetChunkedString() throws Exception {
        runTest(BASE_PATH);
    }

    @Test
    public void testGetStringAsync() throws Exception {
        runTest(BASE_PATH + "/async");
    }

    @Test
    public void testGetStream() throws Exception {

        RequestOptions options = new RequestOptions().setPauseResponse(true);

        getWhenHttpClient().requestAbs(HttpMethod.GET, BASE_PATH + "/stream", options)
                .then(response -> {
                    assertEquals(200, response.statusCode());
                    assertNull(response.headers().get(HttpHeaderNames.TRANSFER_ENCODING));
                    assertEquals("36", response.headers().get(HttpHeaderNames.CONTENT_LENGTH));
                    return getWhenHttpClient().body(response);
                })
                .then(body -> {
                    String text = body.toString();
                    assertEquals("abcdefghijklmnopqrstuvwxyz0123456789", text);
                    testComplete();
                    return null;
                })
                .otherwise(this::onRejected);

        await();
    }
}