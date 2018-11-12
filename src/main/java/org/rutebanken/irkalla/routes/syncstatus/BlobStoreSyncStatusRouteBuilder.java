package org.rutebanken.irkalla.routes.syncstatus;

import com.google.cloud.storage.Storage;
import org.apache.camel.Exchange;
import org.apache.commons.io.IOUtils;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.rutebanken.irkalla.routes.BaseRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Profile("gcs-blobstore")
public class BlobStoreSyncStatusRouteBuilder extends BaseRouteBuilder {


    @Value("${blobstore.gcs.credential.path}")
    private String credentialPath;

    @Value("${blobstore.gcs.project.id}")
    private String projectId;


    @Value("${blobstore.gcs.container.name}")
    private String containerName;

    @Value("${blobstore.gcs.sync.status.blob.name:StopPlace/syncstatus}")
    private String blobName;

    private Storage storage;

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXXX";

    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);


    @Override
    public void configure() throws Exception {
        super.configure();

        storage = BlobStoreHelper.getStorage(credentialPath, projectId);

        from("direct:getSyncStatusUntilTime")
                .process(this::getSyncStatusUntilTime)
                .routeId("get-sync-status-until");

        from("direct:setSyncStatusUntilTime")
                .process(this::setSyncStatusUntilTime)
                .routeId("set-sync-status-until");
    }


    private void getSyncStatusUntilTime(Exchange exchange) {
        Long syncStatusUntilTime = null;

        try {
            InputStream inputStream = BlobStoreHelper.getBlob(storage, containerName, blobName);
            if (inputStream != null) {
                String stringVal = IOUtils.toString(inputStream);
                syncStatusUntilTime = Instant.from(FORMATTER.parse(stringVal)).toEpochMilli();
            } else {
                log.info("Got empty inputstream for syncedUntilTime, assuming this is initial sync.");
            }
        } catch (Exception ex) {
            log.warn("Failed to parse sync status timestamp from gcs, using null. Msg: " + ex.getMessage(), ex);

        }
        exchange.getIn().setBody(syncStatusUntilTime);
    }

    private void setSyncStatusUntilTime(Exchange exchange) {
        String stringVal = Instant.ofEpochMilli(exchange.getIn().getBody(Long.class)).atZone(ZoneId.of("UTC")).format(FORMATTER);

        InputStream inputStream = IOUtils.toInputStream(stringVal);

        BlobStoreHelper.uploadBlobWithRetry(storage, containerName, blobName, inputStream, false);
    }
}
