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

package org.rutebanken.irkalla.routes;

import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spring.SpringRouteBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.entur.pubsub.camel.EnturGooglePubSubConstants;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

import static org.rutebanken.irkalla.Constants.SINGLETON_ROUTE_DEFINITION_GROUP_NAME;

public abstract class BaseRouteBuilder extends SpringRouteBuilder {


    @Value("${irkalla.camel.redelivery.max:3}")
    private int maxRedelivery;

    @Value("${irkalla.camel.redelivery.delay:5000}")
    private int redeliveryDelay;

    @Value("${irkalla.camel.redelivery.backoff.multiplier:3}")
    private int backOffMultiplier;


    @Override
    public void configure() throws Exception {
        errorHandler(defaultErrorHandler()
                .redeliveryDelay(redeliveryDelay)
                .maximumRedeliveries(maxRedelivery)
                .onRedelivery(this::logRedelivery)
                .useExponentialBackOff()
                .backOffMultiplier(backOffMultiplier)
                .logExhausted(true)
                .logRetryStackTrace(true));

    }

    protected void logRedelivery(Exchange exchange) {
        int redeliveryCounter = exchange.getIn().getHeader("CamelRedeliveryCounter", Integer.class);
        int redeliveryMaxCounter = exchange.getIn().getHeader("CamelRedeliveryMaxCounter", Integer.class);
        Throwable camelCaughtThrowable = exchange.getProperty("CamelExceptionCaught", Throwable.class);
        Throwable rootCause = ExceptionUtils.getRootCause(camelCaughtThrowable);

        String rootCauseType = rootCause != null ? rootCause.getClass().getName() : "";
        String rootCauseMessage = rootCause != null ? rootCause.getMessage() : "";

        log.warn("Exchange failed ({}: {}) . Redelivering the message locally, attempt {}/{}...", rootCauseType, rootCauseMessage, redeliveryCounter, redeliveryMaxCounter);
    }

    /**
     * Create a new singleton route definition from URI. Only one such route should be active throughout the cluster at any time.
     */
    protected RouteDefinition singletonFrom(String uri) {
        return this.from(uri).group(SINGLETON_ROUTE_DEFINITION_GROUP_NAME);
    }


    /**
     * Add ACK/NACK completion callback for an aggregated exchange.
     * The callback should be added after the aggregation is complete to prevent individual messages from being acked
     * by the aggregator.
     */
    protected void addOnCompletionForAggregatedExchange(Exchange exchange) {

        List<Message> messages = (List<Message>) exchange.getIn().getBody(List.class);
        List<BasicAcknowledgeablePubsubMessage> ackList = messages.stream()
                .map(m -> m.getHeader(EnturGooglePubSubConstants.ACK_ID, BasicAcknowledgeablePubsubMessage.class))
                .collect(Collectors.toList());

        exchange.addOnCompletion(new Synchronization() {

            @Override
            public void onComplete(Exchange exchange) {
                ackList.stream().forEach(e -> e.ack());
            }

            @Override
            public void onFailure(Exchange exchange) {
                ackList.stream().forEach(e -> e.nack());
            }
        });
    }
}
