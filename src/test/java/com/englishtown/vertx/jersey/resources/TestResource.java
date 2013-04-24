package com.englishtown.vertx.jersey.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/23/13
 * Time: 1:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("test")
public class TestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
        return "{}";
    }

}
