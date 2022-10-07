package org.rutebanken.irkalla.routes.kafka;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.domain.CrudAction;
import org.rutebanken.irkalla.domain.EntityChangedEvent;
import org.rutebanken.irkalla.routes.RouteBuilderIntegrationTestBase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
@CamelSpringBootTest
@Disabled
public class KafkaStopPlaceChangeRouteBuilderTest extends RouteBuilderIntegrationTestBase {
    @Produce("direct:processStopPlacesChangesKafka")
    protected ProducerTemplate changeLogKafka;

    @EndpointInject("mock:sendToKafka")
    protected MockEndpoint sendToKafka;

    @Test
    public void sendEventToKafka() throws Exception {

        String stopPlaceId = "NSR:StopPlace:33";
        EntityChangedEvent entityChangedEvent = createEntityChangeEvent(stopPlaceId);

        AdviceWith.adviceWith(context, "process-changed-stop-place-kafka",
                a -> a.interceptSendToEndpoint( "direct:publishToKafkaStopPlacesChangelog")
                        .skipSendToOriginalEndpoint().to("mock:sendToKafka"));


        context.start();
        sendToKafka.expectedMessageCount(1);

        changeLogKafka.sendBodyAndHeader(entityChangedEvent, Constants.HEADER_ENTITY_ID, stopPlaceId);

        sendToKafka.assertIsSatisfied();
    }

    private EntityChangedEvent createEntityChangeEvent(String stopPlaceId) {
        EntityChangedEvent entityChangedEvent = new EntityChangedEvent();
        entityChangedEvent.setEntityType(EntityChangedEvent.EntityType.STOP_PLACE);
        entityChangedEvent.setCrudAction(CrudAction.UPDATE);
        entityChangedEvent.setEntityId(stopPlaceId);
        entityChangedEvent.setEntityVersion(10L);
        entityChangedEvent.setEntityChanged(Instant.now().toEpochMilli());
        entityChangedEvent.setMsgId("dummy-id");

        return entityChangedEvent;
    }
}
