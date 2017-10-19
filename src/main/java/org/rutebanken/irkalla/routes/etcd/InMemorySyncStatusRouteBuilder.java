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

package org.rutebanken.irkalla.routes.etcd;

import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * In memory impl of sync status routes. For testing without etcd.
 */
@Component
@ConditionalOnProperty(name = "sync.status.in.memory", havingValue = "true")
public class InMemorySyncStatusRouteBuilder extends BaseRouteBuilder {

    private Instant stopPlaceSyncedUntil;

    @Override
    public void configure() throws Exception {
        from("direct:getSyncStatusUntilTime")
                .process(e -> e.getIn().setBody(stopPlaceSyncedUntil))
                .routeId("get-sync-status-until");

        from("direct:setSyncStatusUntilTime")
                .process(e -> stopPlaceSyncedUntil = e.getIn().getBody(Instant.class))
                .routeId("set-sync-status-until");
    }
}
