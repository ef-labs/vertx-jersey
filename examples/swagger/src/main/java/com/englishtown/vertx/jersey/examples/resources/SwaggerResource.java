package com.englishtown.vertx.jersey.examples.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.vertx.core.Vertx;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Api
@Path("swagger-test")
public class SwaggerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "GET MyObject", response = MyObject.class)
    public void getAsync(@Suspended final AsyncResponse asyncResponse, @Context Vertx vertx) {
        vertx.runOnContext(aVoid -> {
            MyObject o = new MyObject();
            o.setName("Andy");
            asyncResponse.resume(o);
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "POST MyObject")
    public MyObject postJson(@ApiParam MyObject in) {
        return in;
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
