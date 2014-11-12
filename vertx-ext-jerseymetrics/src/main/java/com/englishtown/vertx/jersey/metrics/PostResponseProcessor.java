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

import io.vertx.core.http.HttpServerResponse;

import org.glassfish.jersey.server.ContainerResponse;

import com.codahale.metrics.Timer;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;

/**
 * Marks the timer context as complete
 */
public class PostResponseProcessor implements VertxPostResponseProcessor {
    /**
     * <p>
     * Provide additional response processing
     * </p>
     *
     * @param vertxResponse  the vert.x http server response
     * @param jerseyResponse the jersey container response
     */
    @Override
    public void process(HttpServerResponse vertxResponse, ContainerResponse jerseyResponse) {
        Object obj = jerseyResponse.getRequestContext().getProperty(RequestProcessor.LAST_BYTE_TIMER_CONTEXT);
        if (obj instanceof Timer.Context) {
            Timer.Context context = (Timer.Context) obj;
            context.stop();
        }
    }
}
