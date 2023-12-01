package org.rutebanken.irkalla.repository;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple memory-based blob store repository for testing purpose.
 */
@Repository
@Profile("in-memory-blobstore")
public class InMemoryBlobStoreRepository implements BlobStoreRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, byte[]> blobs = new HashMap<>();

    @Override
    public InputStream getBlob(String objectName) {
        logger.debug("get blob called in in-memory blob store");
        byte[] data = blobs.get(objectName);
        return (data == null) ? null : new ByteArrayInputStream(data);
    }

    @Override
    public void uploadBlob(String objectName, InputStream inputStream, boolean makePublic) {
        try {
            logger.debug("upload blob called in in-memory blob store");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            blobs.put(objectName, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
