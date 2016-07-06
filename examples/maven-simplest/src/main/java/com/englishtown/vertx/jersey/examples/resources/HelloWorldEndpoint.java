package com.englishtown.vertx.jersey.examples.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Simple hello world endpoint
 */
@Path("/")
public class HelloWorldEndpoint {

    @GET
    public String doGet() {
        return "Hello World!";
    }

}
