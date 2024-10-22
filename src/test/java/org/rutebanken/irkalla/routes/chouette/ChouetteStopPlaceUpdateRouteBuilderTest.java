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
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.junit.jupiter.api.Test;
import org.rutebanken.irkalla.IrkallaApplication;
import org.rutebanken.irkalla.routes.RouteBuilderIntegrationTestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;



@CamelSpringBootTest
@SpringBootTest(classes = IrkallaApplication.class, properties = "irkalla.camel.redelivery.max=0")
class ChouetteStopPlaceUpdateRouteBuilderTest extends RouteBuilderIntegrationTestBase {


    @Produce( "google-pubsub:{{irkalla.pubsub.project.id}}:ChouetteStopPlaceSyncQueue")
    protected ProducerTemplate updateStopPlaces;

    @Value("${chouette.url}")
    private String chouetteUrl;


    @Value("${tiamat.url}")
    private String tiamatUrl;

    @Value("${tiamat.publication.delivery.path:/services/stop_places/netex/changed_in_period}")
    private String publicationDeliveryPath;

    @EndpointInject("mock:chouetteUpdateStopPlaces")
    protected MockEndpoint chouetteUpdateStopPlaces;

    @EndpointInject("mock:tiamatExportChanges")
    protected MockEndpoint tiamatExportChanges;

    @EndpointInject("mock:etcd")
    protected MockEndpoint etcd;

    @EndpointInject("mock:chouetteStopPlaceSyncQueue")
    protected MockEndpoint chouetteStopPlaceSyncQueueMock;

    @Test
    void testUpdateStopPlaces() throws Exception {
        String exportPath = tiamatUrl + publicationDeliveryPath + "*";

        AdviceWith.adviceWith(context, "tiamat-get-batch-of-changed-stop-places-as-netex",
                a -> a.interceptSendToEndpoint(exportPath)
                        .skipSendToOriginalEndpoint().to("mock:tiamatExportChanges"));

        AdviceWith.adviceWith(context,"chouette-synchronize-stop-place-batch", a -> a.weaveByToUri(chouetteUrl + "/chouette_iev/stop_place*")
                .replace().to("mock:chouetteUpdateStopPlaces"));

        AdviceWith.adviceWith(context,"chouette-synchronize-stop-places-init",
                a -> a.interceptSendToEndpoint("direct:getSyncStatusUntilTime")
                        .skipSendToOriginalEndpoint().to("mock:etcd"));

        AdviceWith.adviceWith(context,"chouette-synchronize-stop-places-complete",
                a -> a.interceptSendToEndpoint("direct:setSyncStatusUntilTime")
                        .skipSendToOriginalEndpoint().to("mock:etcd"));


        context.start();
        tiamatExportChanges.expectedMessageCount(2);

        // Two batches waiting

        tiamatExportChanges.whenExchangeReceived(1, e -> {
            e.getIn().setHeader("Link", exportPath);
            e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "200");
        });
        tiamatExportChanges.whenExchangeReceived(2, e -> e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "200"));


        chouetteUpdateStopPlaces.expectedMessageCount(2);

        updateStopPlaces.sendBody("dummy");

        tiamatExportChanges.assertIsSatisfied();
        chouetteUpdateStopPlaces.assertIsSatisfied();
    }


    @Test
    void testUpdateStopPlacesNoChanges() throws Exception {
        String exportPath = tiamatUrl + publicationDeliveryPath + "*";

        AdviceWith.adviceWith(context, "tiamat-get-batch-of-changed-stop-places-as-netex",
                a -> a.interceptSendToEndpoint(exportPath)
                        .skipSendToOriginalEndpoint().to("mock:tiamatExportChanges"));


        context.start();

        tiamatExportChanges.expectedMessageCount(1);
        tiamatExportChanges.whenExchangeReceived(1, e -> e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "204"));


        updateStopPlaces.sendBody("dummy");


        tiamatExportChanges.assertIsSatisfied();
    }

    @Test
    void testUpdateStopPlacesRetryWhenChouetteIsBusy() throws Exception {
        String exportPath = tiamatUrl + publicationDeliveryPath + "*";

        AdviceWith.adviceWith(context, "tiamat-get-batch-of-changed-stop-places-as-netex",
                a -> a.interceptSendToEndpoint(exportPath).skipSendToOriginalEndpoint().to("mock:tiamatExportChanges"));


        AdviceWith.adviceWith(context, "chouette-synchronize-stop-place-batch",
                a -> {
                    a.weaveByToUri(chouetteUrl + "/chouette_iev/stop_place*")
                            .replace().to("mock:chouetteUpdateStopPlaces");

                    a.weaveByToUri("google-pubsub:(.*):ChouetteStopPlaceSyncQueue").replace().to("mock:chouetteStopPlaceSyncQueue");
                }
        );


        tiamatExportChanges.expectedMessageCount(1);
        chouetteUpdateStopPlaces.expectedMessageCount(1);
        chouetteStopPlaceSyncQueueMock.expectedMessageCount(1);

        // One batch waiting
        tiamatExportChanges.whenExchangeReceived(1, e -> e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "200"));

        // Chouette is busy, returning 423 - "locked"
        chouetteUpdateStopPlaces.whenExchangeReceived(1, e -> {
            throw new HttpOperationFailedException(null, 423, null, null, null, null);
        });

        context.start();

        updateStopPlaces.sendBody("dummy");

        tiamatExportChanges.assertIsSatisfied();
        chouetteUpdateStopPlaces.assertIsSatisfied();
        chouetteStopPlaceSyncQueueMock.assertIsSatisfied(20000);
    }
}
