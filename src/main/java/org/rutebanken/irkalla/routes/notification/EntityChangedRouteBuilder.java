package org.rutebanken.irkalla.routes.notification;

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

                .choice()
                .when(simple("${body.entityType} == '" + EntityChangedEvent.EntityType.STOP_PLACE + "'"))
                .to("direct:handleStopPlaceChanged")
                .end()

                .routeId("entity-changed-route");
    }
}
