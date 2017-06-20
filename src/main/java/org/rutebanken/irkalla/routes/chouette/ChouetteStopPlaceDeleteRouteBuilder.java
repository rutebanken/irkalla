package org.rutebanken.irkalla.routes.chouette;

import org.apache.activemq.ScheduledMessage;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.http.common.HttpOperationFailedException;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.rutebanken.irkalla.util.Http4URL.toHttp4Url;

@Component
public class ChouetteStopPlaceDeleteRouteBuilder extends BaseRouteBuilder {
    @Value("${chouette.url}")
    private String chouetteUrl;

    @Value("${chouette.sync.stop.place.retry.delay:15000}")
    private int retryDelay;

    @Override
    public void configure() throws Exception {
        super.configure();

        singletonFrom("activemq:queue:ChouetteStopPlaceDeleteQueue?transacted=true")
                .transacted()

                .log(LoggingLevel.INFO, "Delete stop place ${header." + Constants.HEADER_ENTITY_ID + "} in Chouette")
                .setBody(constant(null))
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.DELETE))
                .doTry()
                .toD(toHttp4Url(chouetteUrl) + "/chouette_iev/stop_place/${header." + Constants.HEADER_ENTITY_ID + "}")
                .log(LoggingLevel.INFO, "Finished deleting stop place ${header." + Constants.HEADER_ENTITY_ID + "} in Chouette")

                .doCatch(HttpOperationFailedException.class).onWhen(exchange -> {
                    HttpOperationFailedException ex = exchange.getException(HttpOperationFailedException.class);
                     return (ex.getStatusCode() == 423);
                })
                .log(LoggingLevel.INFO, "Unable to delete stop place because Chouette is busy, retry in " + retryDelay + " ms")
                .setHeader(ScheduledMessage.AMQ_SCHEDULED_DELAY, constant(retryDelay))
                .setBody(constant(null))
                .to("activemq:queue:ChouetteStopPlaceSyncQueue")
                .end()
                .routeId("chouette-delete-stop-place");


    }
}
