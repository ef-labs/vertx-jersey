/*
 * The MIT License (MIT)
 * Copyright © 2013 Englishtown <opensource@englishtown.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.englishtown.vertx.jersey.resources;

import com.englishtown.vertx.jersey.integration.MyObject;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.glassfish.jersey.server.ChunkedOutput;
import org.glassfish.jersey.server.JSONP;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Test jersey resource
 */
@Path("test")
public class TestResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHelloWorld() {
        return "Hello world";
    }

    @GET
    @JSONP(queryParam = "cb")
    @Path("json")
    @Produces({MediaType.APPLICATION_JSON, "application/javascript"})
    public String getJson() {
        return "{}";
    }

    @GET
    @Path("html")
    @Produces(MediaType.TEXT_HTML)
    public Response getHtml() throws Exception {
        URI uri = this.getClass().getResource("/web/index.html").toURI();
        File file = new File(uri);
        if (!file.exists()) {
            throw new RuntimeException("File web/index.html does not exist");
        }
        return Response.ok(file).build();
    }

    @POST
    @Path("json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void postJson(
            final MyObject obj,
            @Suspended final AsyncResponse response,
            @Context Vertx vertx) {

        vertx.runOnContext((aVoid) -> {
            if (obj == null) {
                throw new RuntimeException();
            }
            MyObject obj2 = new MyObject();
            obj2.setName("async response");
            response.resume(obj2);
        });
    }

    @POST
    @Path("jsonarray")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void postJsonArray(
            final JsonArray jsonArr,
            @Suspended final AsyncResponse response,
            @Context Vertx vertx) {

        vertx.runOnContext((aVoid) -> {
            if (jsonArr == null) {
                throw new RuntimeException();
            }
            JsonArray jsonArr2 = new JsonArray();
            jsonArr2.add("async response");
            response.resume(jsonArr2);
        });

    }

    @POST
    @Path("jsonobject")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void postJsonObject(
            final JsonObject jsonObj,
            @Suspended final AsyncResponse response,
            @Context Vertx vertx) {

        vertx.runOnContext((aVoid) -> {
            if (jsonObj == null) {
                throw new RuntimeException();
            }
            JsonObject jsonObj2 = new JsonObject();
            jsonObj2.put("name", "async response");
            response.resume(jsonObj2);
        });

    }

    @GET
    @Path("chunked")
    @Produces(MediaType.TEXT_PLAIN)
    public void getChunked(
            @Suspended final AsyncResponse response,
            @Context final Vertx vertx) {

        vertx.runOnContext(aVoid1 -> {

            final ChunkedOutput<String> chunkedOutput = new ChunkedOutput<>(String.class);

            response.resume(chunkedOutput);

            vertx.runOnContext(aVoid2 -> {
                String s = "aaaaaaaaaa";
                try {
                    chunkedOutput.write(s);
                    chunkedOutput.write(s);
                    chunkedOutput.write(s);
                    chunkedOutput.close();
                } catch (IOException e) {
                }
            });
        });

    }

}
