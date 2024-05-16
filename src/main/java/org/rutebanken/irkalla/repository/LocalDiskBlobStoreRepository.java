package org.rutebanken.irkalla.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Simple file-based blob store repository for testing purpose.
 */
@Component
@Profile("local-disk-blobstore")
public class LocalDiskBlobStoreRepository implements BlobStoreRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${blobstore.local.folder:files/blob}")
    private String baseFolder;


    @Override
    public InputStream getBlob(String objectName) {
        logger.debug("get blob called in local-disk blob store on {}", objectName);
        Path path = Paths.get(baseFolder).resolve(objectName);
        if (!path.toFile().exists()) {
            logger.debug("getBlob(): File not found in local-disk blob store: {}", path);
            return null;
        }
        logger.debug("getBlob(): File found in local-disk blob store: {}", path);
        try {
            // converted as ByteArrayInputStream so that Camel stream cache can reopen it
            // since ByteArrayInputStream.close() does nothing
            return new ByteArrayInputStream(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadBlob(String objectName, InputStream inputStream, boolean makePublic) {
        logger.debug("Upload blob called in local-disk blob store on {}", objectName);
        try {
            Path localPath = Paths.get(objectName);

            Path folder = Paths.get(baseFolder).resolve(localPath.getParent());
            Files.createDirectories(folder);

            Path fullPath = Paths.get(baseFolder).resolve(localPath);
            Files.deleteIfExists(fullPath);

            Files.copy(inputStream, fullPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
