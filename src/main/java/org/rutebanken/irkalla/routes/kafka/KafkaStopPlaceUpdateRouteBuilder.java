package org.rutebanken.irkalla.routes.kafka;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.http.common.HttpMethods;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.rutebanken.irkalla.routes.tiamat.netex.PublicationDeliverySplitter;
import org.rutebanken.irkalla.util.Http4URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.rutebanken.irkalla.Constants.HEADER_NEXT_BATCH_URL;

@Component
public class KafkaStopPlaceUpdateRouteBuilder  extends BaseRouteBuilder {

    @Value("${tiamat.url}")
    private String tiamatUrl;

    @Value("${tiamat.publication.delivery.path.stop.place.id:/services/stop_places/netex}")
    private String publicationDeliveryPath;

    @Value("${irkalla.stop.place.kafka.topic.name:}")
    private String kafkaStopPlaceTopic;


    @Autowired
    private KafkaPublisher publisher;

    @Autowired
    private PublicationDeliverySplitter publicationDeliverySplitter;

    @Override
    public void configure() throws Exception {
        super.configure();


        singletonFrom("entur-google-pubsub:KafkaStopPlaceSyncQueue")
                .log(LoggingLevel.INFO,"Stream StopPlace ${header." + Constants.HEADER_ENTITY_ID + "} to Kafka")
                .to("direct:processChangedStopPlacesAsNetexKafka")
                .routeId("kafka-synchronize-stop-places-route");




        from("direct:processChangedStopPlacesAsNetexKafka")
                .log(LoggingLevel.INFO,"Process changed StopPlace ${header." + Constants.HEADER_ENTITY_ID + "} to Kafka")
                .process(this::setTiamatUrl)
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.GET))
                .setBody(constant(null))
                .toD("${header." + HEADER_NEXT_BATCH_URL + "}")
                .choice()
                .when(simple("${header." + Exchange.HTTP_RESPONSE_CODE + "} == 200"))
                .setHeader("stopPlaceId", simple("${header." + Constants.HEADER_ENTITY_ID + "}"))
                .bean(publicationDeliverySplitter,"transform(${body}, ${header.stopPlaceId})")
                .to("direct:publishToKafkaUpdatedStopPlaces")
                .routeId("process-changed-stop-place-netex-kafka");


        from("direct:publishToKafkaUpdatedStopPlaces")
                .log(LoggingLevel.INFO,"Publish StopPlace ${header." + Constants.HEADER_ENTITY_ID + "} to Kafka")
                .choice().when(e -> !StringUtils.isEmpty(kafkaStopPlaceTopic))
                .setHeader("topic", simple(kafkaStopPlaceTopic))
                .bean(publisher, "publishToKafka(${header.topic}, ${body}, ${headers})")
                .endChoice()
                .routeId("tiamat-updated-stop-place-kafka-producer");



    }


    private void setTiamatUrl(Exchange e) {

        final JerseyUriBuilder uriBuilder = new JerseyUriBuilder().path(Http4URL.toHttp4Url(tiamatUrl) + publicationDeliveryPath);
        String stopPlaceId = e.getIn().getHeader(Constants.HEADER_ENTITY_ID, String.class);
        uriBuilder.queryParam("idList", stopPlaceId);
        uriBuilder.queryParam("topographicPlaceExportMode", "NONE");
        uriBuilder.queryParam("tariffZoneExportMode", "NONE");
        uriBuilder.queryParam("versionValidity","ALL");

        e.getIn().setHeader(HEADER_NEXT_BATCH_URL, uriBuilder.build().toString());
    }

}
