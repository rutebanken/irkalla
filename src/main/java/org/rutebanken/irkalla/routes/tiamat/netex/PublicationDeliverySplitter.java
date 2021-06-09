package org.rutebanken.irkalla.routes.tiamat.netex;

import org.entur.netex.NetexParser;

import org.rutebanken.netex.model.GroupOfStopPlaces;
import org.rutebanken.netex.model.GroupsOfStopPlacesInFrame_RelStructure;
import org.rutebanken.netex.model.ModificationEnumeration;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.PublicationDeliveryStructure;

import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.netex.model.StopPlace;
import org.rutebanken.netex.model.StopPlacesInFrame_RelStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/*
Split publication delivery to into multiple publication deliveries
 */
@Component
public class PublicationDeliverySplitter {

    public static final Logger logger= LoggerFactory.getLogger(PublicationDeliverySplitter.class);

public List<PublicationDeliveryStructure> transform(InputStream publicationDelivery) {
    logger.info("Splitting publication delivery.");
    var netexParser = new NetexParser();
    var netexEntityIndex = netexParser.parse(publicationDelivery);
    final Set<String> stopPlaceIds = netexEntityIndex.getStopPlaceIndex().getAllVersions().keySet();
    logger.info("Found {} stop versions in publication delivery",stopPlaceIds.size());

    List<StopPlace> stopPlaces = new ArrayList<>();

    for (String stopPlaceId : stopPlaceIds) {
        //For stopPlaces without validity e.g. Child stops just get current version
        final Optional<StopPlace> childStops = netexEntityIndex.getStopPlaceIndex().getAllVersions(stopPlaceId)
                .stream()
                .filter(stopPlace -> stopPlace.getValidBetween().isEmpty())
                .max(Comparator.comparingLong(s -> Long.parseLong(s.getVersion())));

        childStops.ifPresent(stopPlaces::add);

        final List<StopPlace> filteredStopPlaces = netexEntityIndex.getStopPlaceIndex().getAllVersions(stopPlaceId).stream()
                .filter(stopPlace -> !stopPlace.getValidBetween().isEmpty())
                .sorted(Collections.reverseOrder(Comparator.comparingLong(s -> Long.parseLong(s.getVersion()))))
                .limit(2L)
                .collect(Collectors.toList());

        if (!filteredStopPlaces.isEmpty()) {
            stopPlaces.addAll(filteredStopPlaces);
        }
    }

    List<PublicationDeliveryStructure> publicationDeliveryStructureList = stopPlaces.stream()
            .map(this::createPublicationDeliveryWithStopPlace)
            .collect(Collectors.toList());

    //Group of Stops

    final Optional<GroupOfStopPlaces> groupOfStopPlaces = netexEntityIndex.getGroupOfStopPlacesIndex().getAll().stream()
            .max(Comparator.comparingLong(gosp -> Long.parseLong(gosp.getVersion())));

    groupOfStopPlaces.ifPresent(gosp -> publicationDeliveryStructureList.add(createPublicationDeliveryWithGroupOfStopPlaces(gosp)));

    logger.info("number of publication deliveries {}",publicationDeliveryStructureList.size());
    return publicationDeliveryStructureList;
}

    @SuppressWarnings("unchecked")
    private PublicationDeliveryStructure createPublicationDeliveryWithStopPlace(StopPlace stopPlace) {
        var publicationDeliveryStructure = createPublicationDeliveryStructure();
        var siteFrame = createSiteFrame();

        siteFrame.withStopPlaces(new StopPlacesInFrame_RelStructure()
                .withStopPlace(stopPlace));

        return publicationDeliveryStructure.withDataObjects(
                new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(new ObjectFactory().createSiteFrame(siteFrame)));
    }

    @SuppressWarnings("unchecked")
    private PublicationDeliveryStructure createPublicationDeliveryWithGroupOfStopPlaces(GroupOfStopPlaces groupOfStopPlaces) {
        var publicationDeliveryStructure = createPublicationDeliveryStructure();
        var siteFrame = createSiteFrame();

        siteFrame.withGroupsOfStopPlaces(new GroupsOfStopPlacesInFrame_RelStructure()
        .withGroupOfStopPlaces(groupOfStopPlaces));

        return publicationDeliveryStructure.withDataObjects(
                new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(new ObjectFactory().createSiteFrame(siteFrame)));
    }

    private PublicationDeliveryStructure createPublicationDeliveryStructure() {
        return new PublicationDeliveryStructure()
                .withVersion("1.12:NO-NeTEx-stops:1.4")
                .withPublicationTimestamp(LocalDateTime.now())
                .withParticipantRef("NSR");
    }

    private SiteFrame createSiteFrame() {
        return new ObjectFactory().createSiteFrame()
                .withModification(ModificationEnumeration.NEW)
                .withVersion("1")
                .withId("NSR:SiteFrame:1");
    }
}
