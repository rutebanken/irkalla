package org.rutebanken.irkalla.routes.kafka;

import org.apache.camel.Header;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.avro.EnumType;
import org.rutebanken.irkalla.avro.StopPlaceChangelogEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;

@Service
public class StopPlaceChangeEventFactory {

    public StopPlaceChangelogEvent createStopPlaceChangelogEvent(@Header(value = Constants.HEADER_ENTITY_ID) String stopPlaceId,
                                                                         @Header(value = Constants.HEADER_ENTITY_VERSION) long stopPlaceVersion,
                                                                         @Header(value = Constants.HEADER_CRUD_ACTION) EnumType eventType
                                                                 ) {
        Assert.notNull(stopPlaceId,"stopPlaceId was null");
        Assert.notNull(stopPlaceVersion, "stopPlaceVersion was null");
        Assert.notNull(eventType,"eventType was null");

        return StopPlaceChangelogEvent.newBuilder()
                .setStopPlaceId(stopPlaceId)
                .setStopPlaceVersion(stopPlaceVersion)
                .setEventType(eventType)
                .build();
    }
}
