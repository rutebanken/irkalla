/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

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
                .choice()
                .when(simple("${header." + Constants.HEADER_CRUD_ACTION + "} == ${type:org.rutebanken.irkalla.domain.CrudAction.DELETE}"))
                .setBody(constant(""))
                .to("google-pubsub:{{irkalla.pubsub.project.id}}:ChouetteStopPlaceDeleteQueue")
                .otherwise()
                    .bean("stopPlaceDao", "getStopPlaceChange")
                    .choice()
                    .when(body().isNull())
                        .log(LoggingLevel.WARN, "Discarding stop place changed event for unknown stop place:" +
                                                " ${header." + Constants.HEADER_ENTITY_ID + "} " +
                                                "v: ${header." + Constants.HEADER_ENTITY_VERSION + "} ")
                    .otherwise()
                    .process(e -> e.getIn().setHeader("isEffective", isChangeEffective(e)))
                    .bean("stopPlaceChangedToEvent", "toEvent")
                    .convertBodyTo(String.class)
                    .to("google-pubsub:{{nabu.pubsub.project.id}}:CrudEventQueue")
                    .to("direct:triggerStopPlaceSyncIfChangeIsEffective")
                    .endChoice()
                .end()
                .routeId("tiamat-stop-place-changed");

        from("direct:triggerStopPlaceSyncIfChangeIsEffective")
                .choice()
                .when(simple("${header.isEffective}"))
                .setBody(constant(""))
                .to("google-pubsub:{{irkalla.pubsub.project.id}}:ChouetteStopPlaceSyncQueue")
                .routeId("tiamat-trigger-chouette-update-for-changed-stop");

    }

    private boolean isChangeEffective(Exchange e) {
        Instant changeTime = e.getIn().getBody(StopPlaceChange.class).getChangeTime();
        return changeTime != null && changeTime.isBefore(Instant.now());
    }


}
