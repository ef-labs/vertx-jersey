package com.englishtown.vertx.jersey.features.jackson.integration;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Test jersey resource
 */
@Path("test")
public class JacksonResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject jsonObject(JsonObject json) {
        return json;
    }

    @POST
    @Path("array")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public JsonArray jsonArray(JsonArray json) {
        return json;
    }

    @POST
    @Path("obj")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MyObject object(MyObject obj) {
        return obj;
    }

    private static class MyObject {

        private String prop1;
        private Integer prop2;
        private String propWithCamelCasingToReplace;

        public String getProp1() {
            return prop1;
        }

        public MyObject setProp1(String prop1) {
            this.prop1 = prop1;
            return this;
        }

        public Integer getProp2() {
            return prop2;
        }

        public MyObject setProp2(Integer prop2) {
            this.prop2 = prop2;
            return this;
        }

        public String getPropWithCamelCasingToReplace() {
            return propWithCamelCasingToReplace;
        }

        public MyObject setPropWithCamelCasingToReplace(String propWithCamelCasingToReplace) {
            this.propWithCamelCasingToReplace = propWithCamelCasingToReplace;
            return this;
        }
    }

}
