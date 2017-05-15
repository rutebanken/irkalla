package org.rutebanken.irkalla.routes.tiamat;


import org.apache.camel.LoggingLevel;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Process changelog events for stop places:
 *
 * * Look up current and, if relevant, previous version of stop
 * * Analyze change
 * * Build and send CRUD event to event handler (Nabu).
 *
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
                .bean("stopPlaceChangedToEvent", "toEvent")
                .convertBodyTo(String.class)
                .to("activemq:queue:CrudEventQueue")
                .otherwise()
                .log(LoggingLevel.WARN, "Discarding stop place changed event for unknown stop place:" +
                                                " ${header." + Constants.HEADER_ENTITY_ID + "} " +
                                                "v: ${header." + Constants.HEADER_ENTITY_VERSION + "} ")
                .end()
                .routeId("tiamat-stop-place-changed");

    }


}
