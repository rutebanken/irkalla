package org.rutebanken.irkalla.routes.kafka;

import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static javax.xml.bind.JAXBContext.newInstance;

@Service
public class KafkaPublisher {
    private static final String STOP_PLACE_ID_KAFKA_HEADER_NAME = "stopPlaceId" ;
    private final Logger log = LoggerFactory.getLogger(KafkaPublisher.class);

    @Value("${irkalla.kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${irkalla.kafka.security.protocol}")
    private String securityProtocol;

    @Value("${irkalla.kafka.security.sasl.mechanism}")
    private String saslMechanism;

    @Value("${irkalla.kafka.sasl.username}")
    private String saslUsername;

    @Value("${irkalla.kafka.sasl.password}")
    private String saslPassword;

    @Value("${irkalla.kafka.brokers}")
    private String brokers;

    @Value("${irkalla.kafka.clientId:}")
    private String clientId;


    private KafkaProducer producer;

    @PostConstruct
    public void init() {
        if (!kafkaEnabled) {
            return;
        }

        // Using default configuration as suggested by Camel
        var config = new KafkaConfiguration();

        Properties properties = config.createProducerProperties();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
        properties.put(ProducerConfig.RETRIES_CONFIG, "10");
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "100");

        // Security
        if (!StringUtils.isEmpty(saslUsername) && !StringUtils.isEmpty(saslPassword)) {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);

            properties.put(SaslConfigs.SASL_MECHANISM, saslMechanism);
            String jaasConfigContents = "org.apache.kafka.common.security.scram.ScramLoginModule required\nusername=\"%s\"\npassword=\"%s\";";
            properties.put(
                    SaslConfigs.SASL_JAAS_CONFIG,
                    String.format(jaasConfigContents, saslUsername.trim(), saslPassword.trim())
            );
        } else {
            log.info("Skip Kafka-authentication as no user/passowrd is set");
        }
        producer = new KafkaProducer(properties);

    }

    public void publishToKafka(String topicName, List<PublicationDeliveryStructure> netexData, Map<String, String> metadataHeaders) throws JAXBException {
        if (!kafkaEnabled) {
            log.debug("Push to Kafka is disabled, should have pushed update ");
            return;
        }

        if (producer != null && !netexData.isEmpty()) {
            for (PublicationDeliveryStructure deliveryStructure : netexData) {
                var publicationDeliveryToString = publicationDeliveryToString(deliveryStructure);
                final ProducerRecord record = new ProducerRecord(topicName, publicationDeliveryToString);
                if (metadataHeaders.containsKey(STOP_PLACE_ID_KAFKA_HEADER_NAME)) {
                    record.headers().add(STOP_PLACE_ID_KAFKA_HEADER_NAME,
                            metadataHeaders.get(STOP_PLACE_ID_KAFKA_HEADER_NAME)
                                    .getBytes(StandardCharsets.UTF_8)
                    );
                }
                //Fire and forget
                producer.send(record, createCallback());
            }

        }

    }

    private Callback createCallback() {
        return (recordMetadata, e) -> {
            if (e != null) {
                // Failed
                log.warn("Publishing to kafka failed", e);
            } else {
                // Success
               log.info("Publishing to kafka successful");
            }
        };
    }

    private String publicationDeliveryToString(PublicationDeliveryStructure publicationDeliveryStructure) throws JAXBException {
        JAXBContext jaxbContext = newInstance(PublicationDeliveryStructure.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        JAXBElement<PublicationDeliveryStructure> jaxPublicationDelivery = new ObjectFactory().createPublicationDelivery(publicationDeliveryStructure);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        var outputStream = new ByteArrayOutputStream();
        marshaller.marshal(jaxPublicationDelivery, outputStream);

        return outputStream.toString();
    }
}
