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
