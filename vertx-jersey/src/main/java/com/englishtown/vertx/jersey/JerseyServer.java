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

package com.englishtown.vertx.jersey;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;

/**
 * Represents a jersey server running in vert.x
 */
public interface JerseyServer {

    /**
     * Creates a vert.x {@link HttpServer} with a jersey handler
     *
     * @param options http server and jersey configuration settings
     */
    default void init(JerseyOptions options) {
        init(options, null);
    }

    /**
     * Creates a vert.x {@link HttpServer} with a jersey handler
     *
     * @param options     http server and jersey configuration settings
     * @param doneHandler the optional callback for when initialization has completed
     */
    void init(JerseyOptions options, Handler<AsyncResult<HttpServer>> doneHandler);

    /**
     * Allows custom setup during initialization before the http server is listening (add custom routes, etc.)
     *
     * @param handler the handler invoked with the http server
     */
    void setupHandler(Handler<HttpServer> handler);

    /**
     * Returns the JerseyHandler instance for the JerseyServer
     *
     * @return the JerseyHandler instance
     */
    JerseyHandler getHandler();

    /**
     * Returns the internal vert.x {@link io.vertx.core.http.HttpServer}
     *
     * @return the vert.x http server instance
     */
    HttpServer getHttpServer();

    /**
     * Release resources
     */
    void close();

}
