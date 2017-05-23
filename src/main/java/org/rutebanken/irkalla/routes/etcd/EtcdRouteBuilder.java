package org.rutebanken.irkalla.routes.etcd;

import mousio.etcd4j.responses.EtcdErrorCode;
import mousio.etcd4j.responses.EtcdException;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.etcd.EtcdConstants;
import org.rutebanken.irkalla.Constants;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.apache.camel.component.etcd.EtcdConstants.ETCD_KEYS_ACTION_GET;
import static org.apache.camel.component.etcd.EtcdConstants.ETCD_KEYS_ACTION_SET;

@Component
public class EtcdRouteBuilder extends BaseRouteBuilder {

    @Value("${etcd.url}")
    private String etcdUrl;

    @Value("${etcd.sync.status.prefix:dynamic/irkalla/stop_place/sync}")
    private String etcdSyncStatusPrefix;


    @Override
    public void configure() throws Exception {

        from("direct:getSyncStatusUntilTime")
                .setHeader(EtcdConstants.ETCD_ACTION, constant(ETCD_KEYS_ACTION_GET))
                .setHeader(EtcdConstants.ETCD_PATH, simple(etcdSyncStatusPrefix))
                .setHeader(EtcdConstants.ETCD_DEFAULT_URIS, constant(etcdUrl))
                .doTry()
                .to("etcd:keys")
                .doCatch(EtcdException.class).onWhen(exchange -> {
            EtcdException ex = exchange.getException(EtcdException.class);
            return (ex.errorCode == EtcdErrorCode.KeyNotFound);
        })
                .log(LoggingLevel.INFO, "No synced until date found in etcd")
                .end()
                .setBody(simple("${body.node.value}"))
                .routeId("get-sync-status-until");

        from("direct:setSyncStatusUntilTime")
                .setHeader(EtcdConstants.ETCD_ACTION, constant(ETCD_KEYS_ACTION_SET))
                .setHeader(EtcdConstants.ETCD_PATH, simple(etcdSyncStatusPrefix))
                .setHeader(EtcdConstants.ETCD_DEFAULT_URIS, constant(etcdUrl))
                .to("etcd:keys")
                .routeId("set-sync-status-until");
    }
}
