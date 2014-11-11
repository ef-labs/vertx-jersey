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

package com.englishtown.vertx.jersey.metrics.integration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.test.core.VertxTestBase;

import java.util.SortedMap;

import org.junit.Ignore;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.englishtown.vertx.jersey.metrics.RequestProcessor;

/**
 * Integration tests for jersey metrics
 */
public class BasicIntegrationTest extends VertxTestBase {

    // Had to mark this test as ignored now that SharedMetricRegistries is no longer used.
    // Not sure it is possible to get at the HK2 container's instance of MetricRegistry from here...
    @Ignore
    @Test
    public void testJerseyMetrics() throws Exception {
        HttpClientOptions clientOptions = new HttpClientOptions();
        clientOptions.setConnectTimeout(300);
        vertx.createHttpClient(clientOptions).exceptionHandler(throwable -> {
            fail(throwable.getMessage());
        })
                .request(HttpMethod.GET, 8080, "localhost", "/", response -> {
                    assertEquals(200, response.statusCode());

                    // MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricsBinder.SHARED_REGISTRY_NAME);
                    MetricRegistry registry = new MetricRegistry();
                    SortedMap<String, Timer> timers = registry.getTimers();

                    Timer firstByteTimer = timers.get(RequestProcessor.FIRST_BYTE_TIMER_NAME);
                    Timer lastByteTimer = timers.get(RequestProcessor.LAST_BYTE_TIMER_NAME);

                    assertNotNull(firstByteTimer);
                    assertNotNull(lastByteTimer);
                    assertEquals(1, firstByteTimer.getCount());
                    assertEquals(1, lastByteTimer.getCount());

                    testComplete();
                })
        ;

    }
}

class TestVerticle extends AbstractVerticle {
      /**
     * Override this method to signify that start is complete sometime _after_ the start() method has returned
     * This is useful if your verticle deploys other verticles or modules and you don't want this verticle to
     * be considered started until the other modules and verticles have been started.
     *
     * @param startedResult When you are happy your verticle is started set the result
     */
    @Override
    public void start(final Future<Void> startedResult) throws Exception {

        JsonObject config = new JsonObject()
                .put("hk2_binder", "com.englishtown.vertx.jersey.metrics.integration.IntegrationTestBinder")
                .put("port", 8080)
                .put("resources", new JsonArray().add("com.englishtown.vertx.jersey.resources"));
        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(config);
        Vertx.vertx().deployVerticle(JerseyVerticle.class.getName(), options, ar -> {
                if (ar.succeeded()) {
//                    start();
                    startedResult.complete();
                } else {
                    startedResult.fail(ar.cause());
                }
       });

    }
}