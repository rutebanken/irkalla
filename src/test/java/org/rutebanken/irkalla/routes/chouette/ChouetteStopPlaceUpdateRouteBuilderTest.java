package org.rutebanken.irkalla.routes.chouette;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.language.SimpleExpression;
import org.junit.Test;
import org.rutebanken.irkalla.routes.RouteBuilderIntegrationTestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import static org.rutebanken.irkalla.util.Http4URL.toHttp4Url;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ChouetteStopPlaceUpdateRouteBuilderTest extends RouteBuilderIntegrationTestBase {


    @Produce(uri = "activemq:queue:ChouetteStopPlaceSyncQueue")
    protected ProducerTemplate updateForSingleReferential;

    @Produce(uri = "direct:synchronizeStopPlacesForAllReferentials")
    protected ProducerTemplate triggerAllReferentials;

    @Value("${chouette.url}")
    private String chouetteUrl;

    @Value("${etcd.url}")
    private String etcdUrl;

    @Value("${tiamat.url}")
    private String tiamatUrl;

    @Value("${tiamat.publication.delivery.path:/jersey/publication_delivery/changed}")
    private String publicationDeliveryPath;


    @EndpointInject(uri = "mock:chouettGetReferentials")
    protected MockEndpoint chouetteGetReferentials;

    @EndpointInject(uri = "mock:ChouetteStopPlaceSyncQueue")
    protected MockEndpoint mockChouetteStopPlaceSyncQueue;

    @EndpointInject(uri = "mock:chouettUpdateStopPlacesRef1")
    protected MockEndpoint chouetteUpdateStopPlacesRef1;

    @EndpointInject(uri = "mock:tiamatExportChanges")
    protected MockEndpoint tiamatExportChanges;


    @EndpointInject(uri = "mock:etcd")
    protected MockEndpoint etcd;


    @Test
    public void testCreateTriggerMessagesForAllReferentials() throws Exception {

        context.getRouteDefinition("chouette-get-list-of-referentials-to-sync").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(toHttp4Url(chouetteUrl) + "/chouette_iev/admin/referentials")
                        .skipSendToOriginalEndpoint().to("mock:chouettGetReferentials");
            }
        });
        context.getRouteDefinition("chouette-synchronize-stop-places-all-referentials").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint("activemq:queue:ChouetteStopPlaceSyncQueue")
                        .skipSendToOriginalEndpoint().to("mock:ChouetteStopPlaceSyncQueue");
            }
        });

        context.start();

        chouetteGetReferentials.expectedMessageCount(1);
        chouetteGetReferentials.returnReplyBody(new SimpleExpression("[\"ref1\",\"ref2\"]"));

        mockChouetteStopPlaceSyncQueue.expectedMessageCount(2);

        // TODO assert msg?

        triggerAllReferentials.sendBody(null);

        chouetteGetReferentials.assertIsSatisfied();
        mockChouetteStopPlaceSyncQueue.assertIsSatisfied();
    }

    @Test
    public void testUpdateStopPlaceForRefential() throws Exception {
        String exportPath=toHttp4Url(tiamatUrl) + publicationDeliveryPath +"*";

        context.getRouteDefinition("tiamat-get-batch-of-changed-stop-places-as-netex").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(exportPath)
                        .skipSendToOriginalEndpoint().to("mock:tiamatExportChanges");
            }
        });

        context.getRouteDefinition("chouette-synchronize-stop-place-batch").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(toHttp4Url(chouetteUrl) + "/chouette_iev/stop_place/*")
                        .skipSendToOriginalEndpoint().to("mock:chouettUpdateStopPlacesRef1");
            }
        });


        context.getRouteDefinition("get-sync-status-until").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(toHttp4Url(etcdUrl) + "*")
                        .skipSendToOriginalEndpoint().to("mock:etcd");
            }
        });


        context.start();
        tiamatExportChanges.expectedMessageCount(2);

        // Two batches for ref1, no changes for ref2
        tiamatExportChanges.whenExchangeReceived(1, e -> {
            e.getIn().setHeader("Link", exportPath);
            e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "200");
        });
        tiamatExportChanges.whenExchangeReceived(2, e -> e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "200"));


        chouetteUpdateStopPlacesRef1.expectedMessageCount(2);

        updateForSingleReferential.sendBody("refWithTwoBatches");

        tiamatExportChanges.assertIsSatisfied();
        chouetteUpdateStopPlacesRef1.assertIsSatisfied();
    }



    @Test
    public void testUpdateStopPlaceForRefentialNoChanges() throws Exception {
        String exportPath=toHttp4Url(tiamatUrl) + publicationDeliveryPath +"*";

        context.getRouteDefinition("tiamat-get-batch-of-changed-stop-places-as-netex").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(exportPath)
                        .skipSendToOriginalEndpoint().to("mock:tiamatExportChanges");
            }
        });

        context.getRouteDefinition("get-sync-status-until").adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                interceptSendToEndpoint(toHttp4Url(etcdUrl) + "*")
                        .skipSendToOriginalEndpoint().to("mock:etcd");
            }
        });

        context.start();

        tiamatExportChanges.expectedMessageCount(1);
        tiamatExportChanges.whenExchangeReceived(3, e -> e.getIn().setHeader(Exchange.HTTP_RESPONSE_CODE, "204"));


        updateForSingleReferential.sendBody("refWithNoChanges");

        chouetteGetReferentials.assertIsSatisfied();
        tiamatExportChanges.assertIsSatisfied();
    }
}
