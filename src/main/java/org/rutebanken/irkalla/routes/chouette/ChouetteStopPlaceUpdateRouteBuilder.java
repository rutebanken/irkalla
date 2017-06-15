package org.rutebanken.irkalla.routes.chouette;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

import static org.rutebanken.irkalla.util.Http4URL.toHttp4Url;

@Component
public class ChouetteStopPlaceUpdateRouteBuilder extends BaseRouteBuilder {
    @Value("${chouette.url}")
    private String chouetteUrl;


    @Value("${chouette.sync.stop.place.cron:0 0/5 * * * ?}")
    private String cronSchedule;

    @Override
    public void configure() throws Exception {
        super.configure();

        from("quartz2://irkalla/stopPlaceSync?cron=" + cronSchedule + "&trigger.timeZone=Europe/Oslo")
                .autoStartup("{{chouette.sync.stop.place.autoStartup:true}}")
                .log(LoggingLevel.INFO, "Quartz triggers sync of changed stop places.")
                .inOnly("activemq:queue:ChouetteStopPlaceSyncQueue")
                .routeId("chouette-synchronize-stop-places-quartz");

        singletonFrom("activemq:queue:ChouetteStopPlaceSyncQueue?transacted=true&messageListenerContainerFactoryRef=batchListenerContainerFactory")
                .transacted()
                .setBody(constant(null))
                .log(LoggingLevel.INFO, "Synchronizing stop places in Chouette")
                .setHeader(Constants.HEADER_PROCESS_TARGET, constant("direct:synchronizeStopPlaceBatch"))
                .to("direct:getSyncStatusUntilTime")
                .setHeader(Constants.HEADER_SYNC_STATUS_FROM, simple("${body}"))
                .process(e -> e.getIn().setHeader(Constants.HEADER_SYNC_STATUS_TO, Instant.now()))
                .to("direct:processChangedStopPlacesAsNetex")
                .setBody(simple("${header." + Constants.HEADER_SYNC_STATUS_TO + "}"))
                .to("direct:setSyncStatusUntilTime")

                .log(LoggingLevel.INFO, "Finished synchronizing stop places in Chouette")
                .routeId("chouette-synchronize-stop-places");


        from("direct:synchronizeStopPlaceBatch")
                .convertBodyTo(String.class)
                .removeHeaders("CamelHttp*")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST))
                .toD(toHttp4Url(chouetteUrl) + "/chouette_iev/stop_place")
                .routeId("chouette-synchronize-stop-place-batch");

    }

}
