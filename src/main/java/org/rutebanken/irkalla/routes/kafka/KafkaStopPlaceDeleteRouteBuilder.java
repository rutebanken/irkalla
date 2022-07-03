package org.rutebanken.irkalla.routes.kafka;

import org.apache.camel.LoggingLevel;
import org.apache.commons.lang3.StringUtils;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KafkaStopPlaceDeleteRouteBuilder extends BaseRouteBuilder {

    @Value("${irkalla.stop.place.delete.kafka.topic.name:}")
    private String kafkaStopPlaceTopic;

    @Autowired
    private KafkaPublisher publisher;

    @Override
    public void configure() throws Exception {
        super.configure();
        from("master:lockOnKafkaStopPlaceDeleteRoute:google-pubsub:{{irkalla.pubsub.project.id}}:KafkaStopPlaceDeleteQueue")
                .log(LoggingLevel.INFO,"Stream Deleted StopPlace ${header." + Constants.HEADER_ENTITY_ID + "} to Kafka")
                .choice().when(e -> !StringUtils.isEmpty(kafkaStopPlaceTopic))
                .setHeader("topic", simple(kafkaStopPlaceTopic))
                .bean(publisher, "publishToKafka(${header.topic}, ${body}, ${headers})")
                .endChoice()
                .routeId("kafka-synchronize-stop-places-delete-route");

    }


}
