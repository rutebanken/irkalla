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

package org.rutebanken.irkalla.routes.chouette;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.google.pubsub.GooglePubsubConstants;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.RouteBuilderIntegrationTestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;


@SpringBootTest
class ChouetteStopPlaceDeleteRouteBuilderTest extends RouteBuilderIntegrationTestBase {

    @Produce("google-pubsub:{{irkalla.pubsub.project.id}}:ChouetteStopPlaceDeleteQueue")
    protected ProducerTemplate deleteStopPlaces;

    @Value("${chouette.url}")
    private String chouetteUrl;

    @EndpointInject("mock:chouetteDeleteStopPlace")
    protected MockEndpoint chouetteDeleteStopPlace;


    @Test
    void testDeleteStopPlace() throws Exception {

        Map<String, String> headers = Map.of(Constants.HEADER_ENTITY_ID,"stopPlaceId");
        AdviceWith.adviceWith(context, "chouette-delete-stop-place", a -> {
            a.weaveByToUri(chouetteUrl + "/chouette_iev/stop_place/${header." + Constants.HEADER_ENTITY_ID + "}")
                    .replace().to("mock:chouetteDeleteStopPlace");
            a.interceptSendToEndpoint("direct:updateStatus").skipSendToOriginalEndpoint()
                    .to("mock:updateStatus");
        });

        chouetteDeleteStopPlace.expectedMessageCount(1);

        context.start();
        deleteStopPlaces.sendBodyAndHeader("", GooglePubsubConstants.ATTRIBUTES, headers);
        chouetteDeleteStopPlace.assertIsSatisfied();
    }
}
