package com.englishtown.vertx.jersey.examples.resources;

import com.englishtown.promises.Deferred;
import com.englishtown.promises.When;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: adriangonzalez
 * Date: 4/11/13
 * Time: 11:39 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("when")
public class WhenJerseyServerResource {

    private final When when;

    @Inject
    public WhenJerseyServerResource(When when) {
        this.when = when;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void getString(@Suspended AsyncResponse asyncResponse) throws IOException {

        Deferred<String> d = when.defer();

        d.getPromise()
                .then(str -> {
                    asyncResponse.resume(str);
                    return null;
                })
                .otherwise(t -> {
                    asyncResponse.resume(new WebApplicationException(t));
                    return null;
                });

        d.resolve("when.java!");
    }

}