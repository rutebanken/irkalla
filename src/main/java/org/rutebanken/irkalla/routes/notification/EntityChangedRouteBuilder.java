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

package org.rutebanken.irkalla.routes.notification;

import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.domain.EntityChangedEvent;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class EntityChangedRouteBuilder extends BaseRouteBuilder {

    @Override
    public void configure() throws Exception {
        super.configure();

        from("activemq:queue:IrkallaChangelogQueue?transacted=true")
                .transacted()
                .unmarshal().json(JsonLibrary.Jackson, EntityChangedEvent.class)
                .setHeader(Constants.HEADER_ENTITY_ID,simple("${body.entityId}"))
                .setHeader(Constants.HEADER_ENTITY_VERSION,simple("${body.entityVersion}"))
                .setHeader(Constants.HEADER_CRUD_ACTION,simple("${body.crudAction}"))

                .log(LoggingLevel.INFO,"Received changelog event: ${body.crudAction} ${body.entityType} ${body.entityId} v${body.entityVersion}")
                .choice()
                .when(simple("${body.entityType} == '" + EntityChangedEvent.EntityType.STOP_PLACE + "'"))
                .to("direct:handleStopPlaceChanged")
                .end()

                .routeId("entity-changed-route");
    }
}
