/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey.integration;

import com.englishtown.vertx.jersey.resources.TestResource;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpMethod;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 *
 *
 */
public abstract class IntegrationTests extends JerseyIntegrationTestBase {

    private String host = "localhost";
    private int port = 8080;

    protected IntegrationTests(TestServiceLocator locator) {
        super(locator);
    }

    private void verifyResponse(HttpClientResponse response, int status, String contentType, final String expected) {

        assertEquals(status, response.statusCode());
        assertEquals(contentType, response.headers().get(HttpHeaders.CONTENT_TYPE));

        if (expected != null) {
            response.bodyHandler((Buffer body) -> {
                String result = body.toString("UTF-8");
                assertEquals(expected, result);
                testComplete();
            });
        } else {
            testComplete();
        }
    }

    @Test
    public void test_getJson() throws Exception {

        HttpClientRequest request = httpClient.request(HttpMethod.GET, port, host, "/rest/test/json", response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, "{}");
        });

        request.end();
        await();
    }

    @Test
    public void test_getJsonp() throws Exception {

        final String contentType = "application/javascript";

        HttpClientRequest request = httpClient.request(HttpMethod.GET, port, host, "/rest/test/json?cb=foo_cb", response -> {
            verifyResponse(response, 200, contentType, "foo_cb({})");
        });

        request.headers().add(HttpHeaders.ACCEPT, contentType);
        request.end();
        await();
    }

    @Test
    public void test_getHtml() throws Exception {

        HttpClientRequest request = httpClient.request(HttpMethod.GET, port, host, "/rest/test/html", response -> {
            verifyResponse(response, 200, MediaType.TEXT_HTML, null);
        });

        request.end();
        await();
    }

    @Test
    public void test_postJson() throws Exception {

        HttpClientRequest request = httpClient.request(HttpMethod.POST, port, host, "/rest/test/json", response -> {
            verifyResponse(response, 200, MediaType.APPLICATION_JSON, "{\"name\":\"async response\"}");
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        request.end("{\"name\":\"post\"}");
        await();
    }

    @Test
    public void test_postText() throws Exception {

        String body = "Hello world!";
        HttpClientRequest request = httpClient.request(HttpMethod.POST, port, host, "/rest/test/text", response -> {
            verifyResponse(response, 200, MediaType.TEXT_PLAIN, body);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        request.end(body);
        await();
    }

    @Test
    public void test_postXml() throws Exception {

        String body = "<a></a>";
        HttpClientRequest request = httpClient.request(HttpMethod.POST, port, host, "/rest/test/xml", response -> {
            verifyResponse(response, 200, MediaType.TEXT_XML, body);
        });

        request.headers().add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        request.end(body);
        await();
    }

    @Test
    public void test_getChunked() throws Exception {

        HttpClientRequest request = httpClient.request(HttpMethod.GET, port, host, "/rest/test/chunked", response -> {
            verifyResponse(response, 200, MediaType.TEXT_PLAIN, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        });

        request.end();
        await();
    }

    @Test
    public void test_getHeaders() throws Exception {

        HttpClientRequest request = httpClient.request(HttpMethod.GET, port, host, "/rest/test/headers", response -> {
            assertEquals(HttpResponseStatus.OK.code(), response.statusCode());

            List<String> headers = response.headers().getAll(TestResource.HEADER_TEST1);
            assertEquals(2, headers.size());
            assertEquals("a", headers.get(0));
            assertEquals("b", headers.get(1));

            headers = response.headers().getAll(TestResource.HEADER_TEST2);
            assertEquals(1, headers.size());
            assertEquals("c", headers.get(0));

            assertEquals("a", response.getHeader(TestResource.HEADER_TEST1));
            assertEquals("c", response.getHeader(TestResource.HEADER_TEST2));

            testComplete();
        });

        request.end();
        await();

    }

    public static class HK2 extends IntegrationTests {
        public HK2() {
            super(new HK2TestServiceLocator());
        }
    }

    public static class Guice extends IntegrationTests {
        public Guice() {
            super(new GuiceTestServiceLocator());
        }
    }
}
