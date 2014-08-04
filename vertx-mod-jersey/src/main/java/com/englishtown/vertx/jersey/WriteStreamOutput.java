package com.englishtown.vertx.jersey;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.streams.WriteStream;

/**
 * Similar behavior to the the Jersey {@link org.glassfish.jersey.server.ChunkedOutput},
 * you can stream your response directly to the underlying vert.x HttpServerResponse
 */
public interface WriteStreamOutput extends WriteStream<WriteStreamOutput> {

    /**
     * Sets the underlying vert.x {@link org.vertx.java.core.http.HttpServerResponse}
     * <p/>
     * For internal support, typically you don't need to call this.
     *
     * @param response   the response object
     * @param endHandler the end handler called when writes are completed
     * @return the current write stream output
     */
    WriteStreamOutput init(HttpServerResponse response, Handler<Void> endHandler);

    /**
     * Flag to indicate if the {@link org.vertx.java.core.http.HttpServerResponse} has been set yet.
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
