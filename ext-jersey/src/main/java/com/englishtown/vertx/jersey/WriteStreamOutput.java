package com.englishtown.vertx.jersey;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.WriteStream;

/**
 * Similar behavior to the the Jersey {@link org.glassfish.jersey.server.ChunkedOutput},
 * you can stream your response directly to the underlying vert.x HttpServerResponse
 */
public interface WriteStreamOutput extends WriteStream<Buffer> {

    /**
     * Sets the underlying vert.x {@link io.vertx.core.http.HttpServerResponse}
     * <p>
     * For internal support, typically you don't need to call this.
     *
     * @param response   the response object
     * @param endHandler the end handler called when writes are completed
     * @return the current write stream output
     */
    WriteStreamOutput init(HttpServerResponse response, Handler<Void> endHandler);

    /**
     * Flag to indicate if the {@link io.vertx.core.http.HttpServerResponse} has been set yet.
     * The response must be set before you write.
     *
     * @return boolean flag
     */
    boolean isResponseSet();

    /**
     * Ends the current http response
     */
    void end();
}
