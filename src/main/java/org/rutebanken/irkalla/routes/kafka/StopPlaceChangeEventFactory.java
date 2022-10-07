package org.rutebanken.irkalla.routes.kafka;

import org.apache.camel.Header;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.avro.EnumType;
import org.rutebanken.irkalla.avro.StopPlaceChangelogEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Optional;

@Service
public class StopPlaceChangeEventFactory {

    public StopPlaceChangelogEvent createStopPlaceChangelogEvent(@Header(value = Constants.HEADER_ENTITY_ID) String stopPlaceId,
                                                                         @Header(value = Constants.HEADER_ENTITY_VERSION) Long stopPlaceVersion,
                                                                         @Header(value = Constants.HEADER_ENTITY_CHANGED) Long stopPlaceChanged,
                                                                         @Header(value = Constants.HEADER_CRUD_ACTION) EnumType eventType
                                                                 ) {
        Assert.notNull(stopPlaceId,"stopPlaceId was null");
        Assert.notNull(stopPlaceVersion, "stopPlaceVersion was null");
        Assert.notNull(eventType,"eventType was null");

        return StopPlaceChangelogEvent.newBuilder()
                .setStopPlaceId(stopPlaceId)
                .setStopPlaceVersion(stopPlaceVersion)
                .setStopPlaceChanged(Optional.ofNullable(stopPlaceChanged).map(Instant::ofEpochMilli).orElse(null))
                .setEventType(eventType)
                .build();
    }
}
