package org.rutebanken.irkalla.routes.notification;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.rutebanken.irkalla.domain.CrudAction;
import org.rutebanken.irkalla.domain.EntityChangedEvent;
import org.rutebanken.irkalla.routes.RouteBuilderIntegrationTestBase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EntityChangedRouteBuilderTest extends RouteBuilderIntegrationTestBase {

    @Produce("google-pubsub:{{irkalla.pubsub.project.id}}:ror.tiamat.changelog")
    protected ProducerTemplate changelog;


    @EndpointInject("mock:handleStopPlaceChanged")
    protected MockEndpoint handleStopPlaceChanged;

    @Test
    public void testStopPlaceEntityChangeRoute() throws Exception {

        String entityId = "NSR:StopPlace:34";
        EntityChangedEvent entityChangedEvent = createEntityChangeEvent(entityId,EntityChangedEvent.EntityType.STOP_PLACE);


        AdviceWith.adviceWith(context, "entity-changed-route",
                a -> a.interceptSendToEndpoint( "direct:handleStopPlaceChanged")
                        .skipSendToOriginalEndpoint().to("mock:handleStopPlaceChanged"));

        context.start();
        handleStopPlaceChanged.expectedMessageCount(1);
        changelog.sendBody(entityChangedEvent.toString());

        handleStopPlaceChanged.assertIsSatisfied();
    }

    @Test
    public void testEntityTypeIsNotStopPlace() throws Exception {
        String entityId = "dummy-id";
        EntityChangedEvent entityChangedEvent = createEntityChangeEvent(entityId,null);


        AdviceWith.adviceWith(context, "entity-changed-route",
                a -> a.interceptSendToEndpoint( "direct:handleStopPlaceChanged")
                        .skipSendToOriginalEndpoint().to("mock:handleStopPlaceChanged"));

        context.start();
        handleStopPlaceChanged.expectedMessageCount(0);
        changelog.sendBody(entityChangedEvent.toString());

        handleStopPlaceChanged.assertIsSatisfied();
    }

    private EntityChangedEvent createEntityChangeEvent(String entityId, EntityChangedEvent.EntityType entityType) {
        EntityChangedEvent entityChangedEvent = new EntityChangedEvent();
        entityChangedEvent.setEntityType(entityType);
        entityChangedEvent.setCrudAction(CrudAction.UPDATE);
        entityChangedEvent.setEntityId(entityId);
        entityChangedEvent.setEntityVersion(11L);
        entityChangedEvent.setMsgId("dummy-id");

        return entityChangedEvent;
    }

}