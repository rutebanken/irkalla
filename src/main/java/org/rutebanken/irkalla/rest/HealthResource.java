package org.rutebanken.irkalla.rest;

import org.rutebanken.irkalla.service.IrkallaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component
@Produces("application/json")
@Path("/health")
public class HealthResource {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IrkallaConfiguration irkallaConfiguration;

    private boolean hasBeenOkOnce = false;

    /**
     *  curl http://localhost:8080/babylon/health/ready
     */
    @GET
    @Path("/ready")
    public Response isReady() {
        if ( true) { // Some relevant test here
            return Response.ok("OK").build();
        } else {
            return Response.serverError()
                       .status(Response.Status.SERVICE_UNAVAILABLE)
                       .entity("XXXX is not available. This disables this service as well.")
                       .build();
        }
    }

    @GET
    @Path("/live")
    public Response isLive() {
        if ( !hasBeenOkOnce ) {
            if ( false ) {
                return Response.status(500).entity("Something is not OK").build();
            }

            hasBeenOkOnce = true;
        }

        return Response.ok("OK").build();
    }
}

