package com.englishtown.vertx.jersey.examples.resources;

import org.vertx.java.core.Vertx;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.streams.ReadStream;
import org.vertx.java.platform.Container;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/9/13
 * Time: 7:36 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("test")
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getString(@Context Vertx vertx,
                            @Context Container container,
                            @Context HttpServerRequest vertxRequest,
                            @Context HttpServerResponse vertxResponse,
                            @Context ReadStream<HttpServerRequest> vertxStream) {

        if (vertx != null && container != null && vertxRequest != null
                && vertxResponse != null && vertxStream != null) {
            return "All vert.x objects successfully injected!!! ";
        } else {
            return "Vert.x objects were not successfully injected!!!";
        }

    }

}