package com.englishtown.vertx.jersey.features.swagger.internal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * Swagger CORS request filter
 */
public class SwaggerCorsFilter implements ContainerResponseFilter {

    public static final String HEADER_ORIGIN = "Origin";
    public static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        // Only handle swagger requests
        String path = requestContext.getUriInfo().getPath();
        if (!"swagger.json".equals(path) && !"swagger.yaml".equals(path) && !"swagger".equals(path)) {
            return;
        }

        // CORS requests must have an Origin header
        String origin = requestContext.getHeaderString(HEADER_ORIGIN);
        if (origin == null) {
            return;
        }

        // Always add CORS allow origin header
        responseContext.getHeaders().add(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*");

    }

}
