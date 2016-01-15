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

package com.englishtown.vertx.jersey.promises;

import com.englishtown.promises.Promise;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.JerseyServer;
import com.englishtown.vertx.jersey.JerseyServerOptions;
import com.englishtown.vertx.jersey.inject.Nullable;
import io.vertx.core.json.JsonObject;

/**
 * When.java wrapper over a {@link com.englishtown.vertx.jersey.JerseyServer}
 */
public interface WhenJerseyServer {

    /**
     * Returns a promise for asynchronously creating a {@link com.englishtown.vertx.jersey.JerseyServer}
     *
     * @return a promise for the server
     */
    default Promise<JerseyServer> createServer() {
        return createServer(null, null);
    }

    /**
     * Returns a promise for asynchronously creating a {@link com.englishtown.vertx.jersey.JerseyServer}
     *
     * @return a promise for the server
     */
    Promise<JerseyServer> createServer(@Nullable JerseyServerOptions options, @Nullable JerseyOptions jerseyOptions);

    /**
     * Deprecated
     *
     * @param config the jersey json configuration
     * @return a promise for the server
     * @deprecated Use overload without config
     */
    @Deprecated
    default Promise<JerseyServer> createServer(JsonObject config) {
        return createServer();
    }

    /**
     * Returns a promise for asynchronously creating a {@link com.englishtown.vertx.jersey.JerseyServer}.
     * The promise type matches the WhenContainer signature to facilitate parallel deployments.
     *
     * @return a promise for an empty string
     */
    Promise<String> createServerSimple();

    /**
     * Deprecated
     *
     * @param config the jersey json configuration
     * @return a promise for an empty string
     * @deprecated Use overload without config
     */
    @Deprecated
    default Promise<String> createServerSimple(JsonObject config) {
        return createServerSimple();
    }

}
