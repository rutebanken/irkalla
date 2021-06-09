package org.rutebanken.irkalla.routes.tiamat.netex;


import org.entur.netex.NetexParser;
import org.entur.netex.index.api.NetexEntitiesIndex;
import org.junit.Assert;

import org.junit.Test;
import org.rutebanken.netex.model.LocaleStructure;
import org.rutebanken.netex.model.LocationStructure;
import org.rutebanken.netex.model.MultilingualString;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.SimplePoint_VersionStructure;
import org.rutebanken.netex.model.SiteFrame;

import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.StopPlacesInFrame_RelStructure;
import org.rutebanken.netex.model.ValidBetween;
import org.rutebanken.netex.model.VersionFrameDefaultsStructure;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;


import java.util.Collection;

import java.util.List;
import java.util.UUID;

import static javax.xml.bind.JAXBContext.newInstance;


public class PublicationDeliverySplitterTest {


    @Test
    public void publicationDeliveryParserTest() throws Exception {
        var validFrom = LocalDateTime.of(2021,5,10,12,30);
        var stopPlaceId = "XYZ:Stopplace:1";

        StopPlace stopPlace1 = new StopPlace()
                .withId(stopPlaceId)
                .withVersion("1")
                .withName(new MultilingualString().withValue("Changed stop1"))
                .withValidBetween(new ValidBetween().withFromDate(validFrom).withToDate(validFrom.plusMinutes(10)))
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(new BigDecimal("59.914353"))
                                .withLongitude(new BigDecimal("10.806387"))));

        StopPlace stopPlace2 = new StopPlace()
                .withId(stopPlaceId)
                .withVersion("2")
                .withName(new MultilingualString().withValue("Changed stop2"))
                .withValidBetween(new ValidBetween().withFromDate(validFrom.plusMinutes(11)).withToDate(validFrom.plusMinutes(20)))
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(new BigDecimal("59.914353"))
                                .withLongitude(new BigDecimal("10.806387"))));

        StopPlace stopPlace3 = new StopPlace()
                .withId(stopPlaceId)
                .withVersion("3")
                .withName(new MultilingualString().withValue("Changed stop3"))
                .withValidBetween(new ValidBetween().withFromDate(validFrom.plusMinutes(21)))
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(new BigDecimal("59.914353"))
                                .withLongitude(new BigDecimal("10.806387"))));

        final PublicationDeliveryStructure publicationDeliveryWithStopPlace = createPublicationDeliveryWithStopPlace(stopPlace1, stopPlace2, stopPlace3);

        JAXBContext jaxbContext = newInstance(PublicationDeliveryStructure.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        JAXBElement<PublicationDeliveryStructure> jaxPublicationDelivery = new ObjectFactory().createPublicationDelivery(publicationDeliveryWithStopPlace);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        marshaller.marshal(jaxPublicationDelivery, outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        final List<PublicationDeliveryStructure> result = new PublicationDeliverySplitter().transform(inputStream);

        Assert.assertEquals(2,result.size());



    }

    @Test
    public void multiModelStops() throws Exception {
        var publicationDelivery = new FileInputStream(new File("src/test/resources/org/rutebanken/irkalla/routes/tiamat/netex/publication-delevery.xml"));

        final List<PublicationDeliveryStructure> result = new PublicationDeliverySplitter().transform(publicationDelivery);

        for (PublicationDeliveryStructure publicationDeliveryStructure : result) {

            final NetexEntitiesIndex parse = new NetexParser().parse(publicationDeliveryToString(publicationDeliveryStructure));
            final Collection<Collection<StopPlace>> values = parse.getStopPlaceIndex().getAllVersions().values();
            for (Collection<StopPlace> value : values) {
                for (StopPlace stopPlace : value) {
                    System.out.println("stopPlaceId: "  + stopPlace.getId() + ", version: "+ stopPlace.getVersion());
                }
            }

        }

        Assert.assertEquals(5,result.size());
    }

    @SuppressWarnings("unchecked")
    private PublicationDeliveryStructure createPublicationDeliveryWithStopPlace(StopPlace... stopPlace) {
        SiteFrame siteFrame = new SiteFrame();
        siteFrame.setVersion("1");
        siteFrame.setId(UUID.randomUUID().toString());
        siteFrame.setFrameDefaults(
                new VersionFrameDefaultsStructure()
                        .withDefaultLocale(
                                new LocaleStructure().withTimeZone("Europe/Paris")));
        siteFrame.withStopPlaces(new StopPlacesInFrame_RelStructure()
                .withStopPlace(stopPlace));

        return new PublicationDeliveryStructure()
                .withPublicationTimestamp(LocalDateTime.now())
                .withVersion("1")
                .withParticipantRef("test")
                .withDataObjects(new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(new ObjectFactory().createSiteFrame(siteFrame)));
    }

    private InputStream publicationDeliveryToString(PublicationDeliveryStructure publicationDeliveryStructure) throws JAXBException {
        JAXBContext jaxbContext = newInstance(PublicationDeliveryStructure.class);
        Marshaller marshaller = jaxbContext.createMarshaller();

        JAXBElement<PublicationDeliveryStructure> jaxPublicationDelivery = new ObjectFactory().createPublicationDelivery(publicationDeliveryStructure);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        var outputStream = new ByteArrayOutputStream();
        marshaller.marshal(jaxPublicationDelivery, outputStream);



        return new ByteArrayInputStream(outputStream.toByteArray());

    }


}