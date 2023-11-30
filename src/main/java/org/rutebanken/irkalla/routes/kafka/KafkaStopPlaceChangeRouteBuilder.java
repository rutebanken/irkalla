package org.rutebanken.irkalla.routes.kafka;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.http.common.HttpMethods;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!no-kafka")
public class KafkaStopPlaceChangeRouteBuilder extends BaseRouteBuilder {

    @Autowired
    private StopPlaceChangeEventFactory stopPlaceChangeEventFactory;

    @Override
    public void configure() throws Exception {
        super.configure();

        from("google-pubsub:{{irkalla.pubsub.project.id}}:KafkaStopPlaceChangelog")
                .log(LoggingLevel.INFO,"Process changed StopPlace ${header." + Constants.HEADER_ENTITY_ID + "} to Kafka")
                .log(LoggingLevel.INFO,"Exchange body is ${body}")
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setHeader("stopPlaceId", simple("${header." + Constants.HEADER_ENTITY_ID + "}"))
                .to("direct:notifyConsumers")
                .routeId("process-changed-stop-place-kafka");


        from("direct:notifyConsumers")
                .log(LoggingLevel.INFO, "Notifying Kafka topic ${properties:irkalla.kafka.topic.event}")
                .bean(stopPlaceChangeEventFactory,"createStopPlaceChangelogEvent")
                .to("kafka:{{irkalla.kafka.topic.event}}?clientId=irkalla-event&valueSerializer=io.confluent.kafka.serializers.KafkaAvroSerializer").id("to-kafka-topic-event")
                .log(LoggingLevel.INFO,   "Notified changelog: ${body}")
                .routeId("notify-consumers");

    }

}
