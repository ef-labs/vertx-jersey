package com.englishtown.vertx.jersey.examples.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("service")
public class ServiceResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String doGet() throws IOException {
        return "Hello world!";
    }

}