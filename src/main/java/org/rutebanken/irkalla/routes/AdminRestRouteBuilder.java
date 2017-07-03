package org.rutebanken.irkalla.routes;

import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestPropertyDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static org.rutebanken.irkalla.Constants.HEADER_FULL_SYNC;

@Component
public class AdminRestRouteBuilder extends BaseRouteBuilder {

    @Value("${server.admin.port}")
    public String port;

    @Value("${server.admin.host}")
    public String host;

    @Override
    public void configure() throws Exception {
        super.configure();

        RestPropertyDefinition corsAllowedHeaders = new RestPropertyDefinition();
        corsAllowedHeaders.setKey("Access-Control-Allow-Headers");
        corsAllowedHeaders.setValue("Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization");

        restConfiguration().setCorsHeaders(Collections.singletonList(corsAllowedHeaders));

        restConfiguration()
                .component("jetty")
                .bindingMode(RestBindingMode.json)
                .enableCORS(true)
                .dataFormatProperty("prettyPrint", "true")
                .host(host)
                .port(port)
                .apiContextPath("/api-doc")
                .apiProperty("api.title", "Irkalla Admin API").apiProperty("api.version", "1.0")

                .contextPath("/admin");

        rest("/stop_places")
                .post("/sync/delta")
                .description("Synchronize new changes for stop places from Tiamat to Chouette")
                .responseMessage().code(200).endResponseMessage()
                .responseMessage().code(500).message("Internal error").endResponseMessage()
                .route().routeId("admin-chouette-synchronize-stop-places-delta")
                .removeHeaders("CamelHttp*")
                .inOnly("activemq:queue:ChouetteStopPlaceSyncQueue")
                .setBody(constant(null))
                .endRest()
                .post("/sync/full")
                .description("Full synchronization of all stop places from Tiamat to Chouette")
                .responseMessage().code(200).endResponseMessage()
                .responseMessage().code(500).message("Internal error").endResponseMessage()
                .route().routeId("admin-chouette-synchronize-stop-places-full")
                .removeHeaders("CamelHttp*")
                .setHeader(HEADER_FULL_SYNC, constant(true))
                .inOnly("activemq:queue:ChouetteStopPlaceSyncQueue")
                .setBody(constant(null))
                .endRest();

    }
}
