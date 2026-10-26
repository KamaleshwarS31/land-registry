package com.landregistry.evidence;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.Duration;

public interface DocumentStorageService {
    StorageUploadResult uploadDocument(MultipartFile file, String parcelCode, String sourceId);
    InputStream downloadDocument(String storagePath);
    String generateAccessUrl(String storagePath, Duration ttl);
    void deleteDocument(String storagePath);
    String getProviderName();
}
