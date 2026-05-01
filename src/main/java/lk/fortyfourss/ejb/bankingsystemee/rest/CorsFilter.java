package lk.fortyfourss.ejb.bankingsystemee.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            requestContext.abortWith(Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .header("Access-Control-Max-Age", "1209600")
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Only add the header if it doesn't already exist!
        if (responseContext.getHeaders().getFirst("Access-Control-Allow-Origin") == null) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        }

        if (responseContext.getHeaders().getFirst("Access-Control-Allow-Headers") == null) {
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        }

        if (responseContext.getHeaders().getFirst("Access-Control-Allow-Methods") == null) {
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}