package org.rutebanken.irkalla.routes.chouette;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.RouteBuilderIntegrationTestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import static org.rutebanken.irkalla.util.Http4URL.toHttp4Url;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChouetteStopPlaceDeleteRouteBuilderTest extends RouteBuilderIntegrationTestBase {


    @Produce(uri = "activemq:queue:ChouetteStopPlaceDeleteQueue")
    protected ProducerTemplate deleteStopPlaces;

    @Value("${chouette.url}")
    private String chouetteUrl;

    @EndpointInject(uri = "mock:chouetteDeleteStopPlace")
    protected MockEndpoint chouetteDeleteStopPlace;

    @Test
    public void testDeleteStopPlace() throws Exception {

        String stopPlaceId = "NSR:StopPlace:33";

        context.getRouteDefinition("chouette-delete-stop-place").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(toHttp4Url(chouetteUrl) + "/chouette_iev/stop_place/NSR:StopPlace:33")
                        .skipSendToOriginalEndpoint().to("mock:chouetteDeleteStopPlace");
            }
        });


        context.start();
        chouetteDeleteStopPlace.expectedMessageCount(1);

        deleteStopPlaces.sendBodyAndHeader(null, Constants.HEADER_ENTITY_ID, stopPlaceId);

        chouetteDeleteStopPlace.assertIsSatisfied();
    }
}
