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

package org.rutebanken.irkalla.routes.tiamat;

import org.apache.camel.Header;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.domain.CrudAction;

public interface StopPlaceDao {

    StopPlaceChange getStopPlaceChange(@Header(value = Constants.HEADER_CRUD_ACTION) CrudAction crudAction,
                                              @Header(value = Constants.HEADER_ENTITY_ID) String id,
                                              @Header(value = Constants.HEADER_ENTITY_VERSION) Long version);
}
