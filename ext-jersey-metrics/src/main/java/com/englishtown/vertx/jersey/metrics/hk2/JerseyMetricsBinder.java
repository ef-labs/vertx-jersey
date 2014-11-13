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

package com.englishtown.vertx.jersey.metrics.hk2;

import com.englishtown.vertx.hk2.MetricsBinder;
import com.englishtown.vertx.jersey.inject.VertxPostResponseProcessor;
import com.englishtown.vertx.jersey.inject.VertxRequestProcessor;
import com.englishtown.vertx.jersey.inject.VertxResponseProcessor;
import com.englishtown.vertx.jersey.metrics.PostResponseProcessor;
import com.englishtown.vertx.jersey.metrics.RequestProcessor;
import com.englishtown.vertx.jersey.metrics.ResponseProcessor;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * HK2 binder for jersey metrics
 */
public class JerseyMetricsBinder extends AbstractBinder {
    /**
     * Implement to provide binding definitions using the exposed binding
     * methods.
     */
    @Override
    protected void configure() {

        // Ensure the metrics binder has run (safe to run multiple times)
        install(new MetricsBinder());

        // Run request processor first so give it a high rank
        bind(RequestProcessor.class).to(VertxRequestProcessor.class).ranked(999);
        // Run response processor last so give it a low rank
        bind(ResponseProcessor.class).to(VertxResponseProcessor.class).ranked(-999);
        // Run post response processor last so give it a low rank
        bind(PostResponseProcessor.class).to(VertxPostResponseProcessor.class).ranked(-999);

    }
}
