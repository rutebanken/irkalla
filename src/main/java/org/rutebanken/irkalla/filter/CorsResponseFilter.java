package org.rutebanken.irkalla.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Provider
public class CorsResponseFilter implements ContainerResponseFilter {
    private static final Logger log = LoggerFactory.getLogger(CorsResponseFilter.class);

    /**
     * Blatantly copied
     */
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        String referer = stripReferer( request.getHeaderString("Referer"));
        if ( referer == null ) {
            referer = "*";
            //log.debug("Referer was null. Setting it to *");
        }
        response.getHeaders().add("Access-Control-Allow-Origin", referer);
        response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");

        response.getHeaders().add("X-Accel-Buffering", "no");
    }

    public static String stripReferer(String referer) {
        if ( referer == null ) {
            return null;
        }
        try {
            URI uri = new URI(referer);
            String portExpr = uri.getPort() == -1
                    ? ""
                    : ":"+uri.getPort();
            return uri.getScheme()+"://"+uri.getHost()+portExpr;
        } catch (URISyntaxException e) {
            log.error("Could not create uri out of "+referer+" Returning empty string for referer", e);
            return "";
        }
    }
}