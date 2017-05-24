package org.rutebanken.irkalla.routes.tiamat;


import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Process changelog events for stop places:
 * <p>
 * * Look up current and, if relevant, previous version of stop
 * * Analyze change
 * * Build and send CRUD event to event handler (Nabu).
 */
@Component
public class TiamatStopPlaceChangedRouteBuilder extends BaseRouteBuilder {

    @Override
    public void configure() throws Exception {
        super.configure();

        from("direct:handleStopPlaceChanged")
                .bean("stopPlaceDao", "getStopPlaceChange")
                .choice()
                .when(body().isNotNull())
                .process(e -> e.getIn().setHeader("isEffective", isChangeEffective(e)))

                .bean("stopPlaceChangedToEvent", "toEvent")
                .convertBodyTo(String.class)
                .to("activemq:queue:CrudEventQueue")
                .to("direct:triggerStopPlaceSyncIfChangeIsEffective")
                .otherwise()
                .log(LoggingLevel.WARN, "Discarding stop place changed event for unknown stop place:" +
                                                " ${header." + Constants.HEADER_ENTITY_ID + "} " +
                                                "v: ${header." + Constants.HEADER_ENTITY_VERSION + "} ")
                .end()
                .routeId("tiamat-stop-place-changed");

        from("direct:triggerStopPlaceSyncIfChangeIsEffective")
                .choice()
                .when(simple("${header.isEffective}"))
                .setBody(constant(null))
                .to("activemq:queue:ChouetteStopPlaceSyncQueue")
                .routeId("tiamat-trigger-chouette-update-for-changed-stop");
    }

    private boolean isChangeEffective(Exchange e) {
        Instant changeTime = e.getIn().getBody(StopPlaceChange.class).getChangeTime();
        return changeTime != null && changeTime.isBefore(Instant.now());
    }


}
