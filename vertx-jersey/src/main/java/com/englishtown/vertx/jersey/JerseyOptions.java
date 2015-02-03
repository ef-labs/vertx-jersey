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

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.KeyStoreOptions;
import org.glassfish.jersey.server.ApplicationHandler;

import java.net.URI;

/**
 * Provides configuration for a {@link JerseyHandler}
 */
public interface JerseyOptions {

    /**
     * Initializes the jersey options
     *
     * @param config the underlying configuration settings
     * @param vertx  the vertx instance
     */
    void init(JsonObject config, Vertx vertx);

    /**
     * Returns the current vertx instance
     *
     * @return the {@link Vertx} instance
     */
    Vertx getVertx();

    /**
     * The http web server host
     *
     * @return the http web server host to listen to
     */
    String getHost();

    /**
     * The http web server port
     *
     * @return the http web server port to listen to
     */
    int getPort();

    /**
     * Whether the web server should be https.
     *
     * @return whether the web server should be https.
     */
    boolean getSSL();

    /**
     * Vert.x http server key store options
     *
     * @return
     */
    KeyStoreOptions getKeyStoreOptions();

    /**
     * The TCP receive buffer size for connections in bytes
     *
     * @return buffer size in bytes
     */
    Integer getReceiveBufferSize();

    /**
     * The accept backlog
     *
     * @return the accept backlog
     */
    int getAcceptBacklog();

    /**
     * Returns the base URI used by Jersey
     *
     * @return base URI
     */
    URI getBaseUri();

    /**
     * Returns the Jersey {@link ApplicationHandler} instance
     *
     * @return the application handler instance
     */
    ApplicationHandlerDelegate getApplicationHandler();

    /**
     * The max body size in bytes when reading the vert.x input stream
     *
     * @return the max body size bytes
     */
    int getMaxBodySize();

}
