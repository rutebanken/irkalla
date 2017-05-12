package org.rutebanken.irkalla.routes.tiamat;

import org.apache.camel.Header;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.domain.ChangeType;

public interface StopPlaceDao {

    StopPlaceChange getStopPlaceChange(@Header(value = Constants.HEADER_CHANGE_TYPE) ChangeType changeType,
                                              @Header(value = Constants.HEADER_ENTITY_ID) String id,
                                              @Header(value = Constants.HEADER_ENTITY_VERSION) Long version);
}
