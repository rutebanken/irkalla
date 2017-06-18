package org.rutebanken.irkalla.routes.chouette;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.http4.HttpMethods;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.rutebanken.irkalla.util.Http4URL.toHttp4Url;

@Component
public class ChouetteStopPlaceDeleteRouteBuilder extends BaseRouteBuilder {
    @Value("${chouette.url}")
    private String chouetteUrl;

    @Override
    public void configure() throws Exception {
        super.configure();

        singletonFrom("activemq:queue:ChouetteStopPlaceDeleteQueue?transacted=true")
                .transacted()

                .log(LoggingLevel.INFO, "Delete stop place ${header." + Constants.HEADER_ENTITY_ID + "} in Chouette")
                .setBody(constant(null))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.DELETE))
                .toD(toHttp4Url(chouetteUrl) + "/chouette_iev/stop_place/${header." + Constants.HEADER_ENTITY_ID + "}")
                .log(LoggingLevel.INFO, "Finished deleting stop place ${header." + Constants.HEADER_ENTITY_ID + "} in Chouette")
                .routeId("chouette-delete-stop-place");


    }
}
