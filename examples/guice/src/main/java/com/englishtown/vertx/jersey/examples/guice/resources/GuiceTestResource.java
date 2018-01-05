package com.englishtown.vertx.jersey.examples.guice.resources;

import com.englishtown.vertx.jersey.examples.guice.MyDependency;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;

/**
 * Guice JAX-RS endpoint
 */
@Singleton
@Path("guice")
public class GuiceTestResource {

    private final MyDependency myDependency;

    @Inject
    public GuiceTestResource(MyDependency myDependency) {
        this.myDependency = myDependency;
    }

    @GET
    public String doGet() {
        return "Instance of " + myDependency.getClass().getName() + " injected!";
    }

    @GET
    @Path("exception")
    public void doException() {
        throw new InternalServerErrorException("Test exception");
    }

}
