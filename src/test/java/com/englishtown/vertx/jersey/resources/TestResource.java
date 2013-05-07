package com.englishtown.vertx.jersey.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Test jersey resource
 */
@Path("test")
public class TestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        return "{}";
    }

}
