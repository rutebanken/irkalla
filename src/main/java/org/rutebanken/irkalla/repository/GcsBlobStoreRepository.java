package org.rutebanken.irkalla.repository;

import com.google.cloud.storage.Storage;
import org.rutebanken.helper.gcp.BlobStoreHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.InputStream;

/**
 * Blob store repository targeting Google Cloud Storage.
 */
@Repository
@Profile("gcs-blobstore")
public class GcsBlobStoreRepository implements BlobStoreRepository {

    @Value("${blobstore.gcs.container.name}")
    private String containerName;

    @Value("${blobstore.gcs.credential.path}")
    private String credentialPath;

    @Value("${blobstore.gcs.project.id}")
    private String projectId;

    private Storage storage;

    @PostConstruct
    private void init() {
        storage = BlobStoreHelper.getStorage(credentialPath, projectId);
    }

    @Override
    public InputStream getBlob(String name) {
        return BlobStoreHelper.getBlob(storage, containerName, name);
    }

    @Override
    public void uploadBlob(String name, InputStream inputStream, boolean makePublic) {
        BlobStoreHelper.uploadBlobWithRetry(storage, containerName, name, inputStream, makePublic);
    }
}
