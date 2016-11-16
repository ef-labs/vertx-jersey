package com.englishtown.vertx.jersey.examples.guice.inject;

import io.vertx.core.json.JsonObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Implementation of {@link javax.ws.rs.ext.ExceptionMapper}
 */
public class GuiceExceptionMapper implements ExceptionMapper<WebApplicationException> {

    public static final Response.StatusType STATUS_IM_A_TEAPOT = new Response.StatusType() {
        @Override
        public int getStatusCode() {
            return 418;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.familyOf(getStatusCode());
        }

        @Override
        public String getReasonPhrase() {
            return "I'm a teapot";
        }
    };

    /**
     * Map an exception to a {@link Response}. Returning
     * {@code null} results in a {@link Response.Status#NO_CONTENT}
     * response. Throwing a runtime exception results in a
     * {@link Response.Status#INTERNAL_SERVER_ERROR} response.
     *
     * @param exception the exception to map to a response.
     * @return a response mapped from the supplied exception.
     */
    @Override
    public Response toResponse(WebApplicationException exception) {
        return Response.status(STATUS_IM_A_TEAPOT)
                .entity(new JsonObject()
                        .put("msg", exception.getMessage())
                        .put("type", exception.getClass().getName())
                        .encode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
