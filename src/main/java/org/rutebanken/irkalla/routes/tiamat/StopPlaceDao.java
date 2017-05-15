package org.rutebanken.irkalla.routes.tiamat;

import org.apache.camel.Header;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.domain.CrudAction;

public interface StopPlaceDao {

    StopPlaceChange getStopPlaceChange(@Header(value = Constants.HEADER_CRUD_ACTION) CrudAction crudAction,
                                              @Header(value = Constants.HEADER_ENTITY_ID) String id,
                                              @Header(value = Constants.HEADER_ENTITY_VERSION) Long version);
}
