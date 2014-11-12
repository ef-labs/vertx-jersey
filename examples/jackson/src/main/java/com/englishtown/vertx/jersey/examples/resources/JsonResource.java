package com.englishtown.vertx.jersey.examples.resources;

import io.vertx.core.Vertx;
import org.glassfish.jersey.server.JSONP;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("json")
public class JsonResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public MyObject getJson() {
        MyObject o = new MyObject();
        o.setName("Andy");
        return o;
    }

    @GET
    @Path("jsonp")
    @JSONP(queryParam = "cb")
    @Produces("application/javascript")
    public MyObject getJsonPadding() {
        MyObject o = new MyObject();
        o.setName("Andy");
        return o;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MyObject postJson(MyObject in) {
        return in;
    }

    @GET
    @Path("async")
    @Produces(MediaType.APPLICATION_JSON)
    public void getJsonAsync(@Suspended final AsyncResponse asyncResponse, @Context Vertx vertx) {
        vertx.runOnContext((Void) -> {
            MyObject o = new MyObject();
            o.setName("Andy");
            asyncResponse.resume(o);
        });
    }

    public static class MyObject {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
