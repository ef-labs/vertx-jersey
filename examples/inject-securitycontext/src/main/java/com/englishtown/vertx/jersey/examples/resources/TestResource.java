package com.englishtown.vertx.jersey.examples.resources;

import io.vertx.core.Vertx;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("test")
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getString() {
        return "Hello World";
    }

    @GET
    @Path("secure")
    @RolesAllowed({"1", "2"})
    @Produces(MediaType.TEXT_PLAIN)
    public String getStringSecure(@Context Vertx vertx) {
        return "Hello World";
    }

}