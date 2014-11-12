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

package com.englishtown.vertx.jersey.metrics;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

import javax.inject.Inject;

import org.glassfish.jersey.server.ContainerRequest;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;

/**
 * Adds metrics for vert.x requests
 */
public class RequestProcessor implements VertxRequestProcessor {

    public static final String FIRST_BYTE_TIMER_CONTEXT = "et.timer.firstByte.context";
    public static final String LAST_BYTE_TIMER_CONTEXT = "et.timer.lastByte.context";

    public static final String FIRST_BYTE_TIMER_NAME = "et.metrics.jersey.firstByte";
    public static final String LAST_BYTE_TIMER_NAME = "et.metrics.jersey.lastByte";

    private final Timer firstByteTimer;
    private final Timer lastByteTimer;

    @Inject
    public RequestProcessor(MetricRegistry registry) {
        firstByteTimer = registry.timer(FIRST_BYTE_TIMER_NAME);
        lastByteTimer = registry.timer(LAST_BYTE_TIMER_NAME);
    }

    /**
     * Provide additional async request processing
     *
     * @param vertxRequest  the vert.x http server request
     * @param jerseyRequest the jersey container request
     * @param done          the done async callback handler
     */
    @Override
    public void process(HttpServerRequest vertxRequest, ContainerRequest jerseyRequest, Handler<Void> done) {
        jerseyRequest.setProperty(FIRST_BYTE_TIMER_CONTEXT, firstByteTimer.time());
        jerseyRequest.setProperty(LAST_BYTE_TIMER_CONTEXT, lastByteTimer.time());
        done.handle(null);
    }
}
