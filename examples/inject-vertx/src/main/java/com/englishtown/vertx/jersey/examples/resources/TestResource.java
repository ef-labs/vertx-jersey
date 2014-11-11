package com.englishtown.vertx.jersey.examples.resources;


import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.ReadStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("test")
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getString(@Context Vertx vertx,
                            @Context HttpServerRequest vertxRequest,
                            @Context HttpServerResponse vertxResponse,
                            @Context ReadStream<HttpServerRequest> vertxStream) {

        if (vertx != null && vertxRequest != null
                && vertxResponse != null && vertxStream != null) {
            return "All vert.x objects successfully injected!!! ";
        } else {
            return "Vert.x objects were not successfully injected!!!";
        }

    }

}
