package org.rutebanken.irkalla.rest;

import org.glassfish.jersey.server.mvc.Viewable;
import org.rutebanken.irkalla.service.IrkallaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Component
@Produces("application/json")
@Path("/")
public class IndexPageResource {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ErrorAttributes errorAttributes;

    @Autowired
    private IrkallaConfiguration configuration;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response redirectSlashToIndex() throws URISyntaxException {
        return Response.seeOther(new URI(configuration.getLinkPrefix()+ "index.html")).build();
    }

    @GET
    @Path("/error")
    @Produces(MediaType.TEXT_PLAIN)
    public Response error(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        ErrorJson err = new ErrorJson(response.getStatus(), getErrorAttributes(request, true));

        return Response.status(response.getStatus()).entity(err.toString()).build();
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    @GET
    @Path("/index.html")
    @Produces(MediaType.TEXT_HTML)
    public Viewable indexPage() {
        final Map<String, Object> map = new HashMap<>();

        map.put("linkPrefix",  configuration.getLinkPrefix());
        map.put("contentPage", "index");
        map.put("title", "welcome to irkalla");

        return new Viewable("/ftl/framework.ftl", map);
    }


    public class ErrorJson {

        private Integer status;
        private String error;
        private String message;
        private String timeStamp;
        private String trace;

        public ErrorJson(int status, Map<String, Object> errorAttributes) {
            this.status = status;
            this.error = (String) errorAttributes.get("error");
            this.message = (String) errorAttributes.get("message");
            this.timeStamp = errorAttributes.get("timestamp").toString();
            this.trace = (String) errorAttributes.get("trace");
        }

        public Integer getStatus() {
            return status;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public String getTrace() {
            return trace;
        }

        @Override
        public String toString() {
            return "ErrorJson{" +
                    "status=" + status +
                    ", error='" + error + '\'' +
                    ", message='" + message + '\'' +
                    ", timeStamp='" + timeStamp + '\'' +
                    ", trace='" + trace + '\'' +
                    '}';
        }
    }

}
