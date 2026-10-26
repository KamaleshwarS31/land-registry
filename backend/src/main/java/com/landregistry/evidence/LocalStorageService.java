package com.landregistry.evidence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "LOCAL", matchIfMissing = true)
public class LocalStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    private final Path rootPath;

    public LocalStorageService(@Value("${app.storage.local-dir:./uploads}") String localDir) {
        this.rootPath = Paths.get(localDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootPath);
            log.info("Initialized LocalStorageService at {}", this.rootPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create local upload directory: " + localDir, e);
        }
    }

    @Override
    public StorageUploadResult uploadDocument(MultipartFile file, String parcelCode, String sourceId) {
        try {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.bin";
            String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String subDir = (parcelCode != null ? parcelCode : "unassigned") + "/" + sourceId;
            Path targetDir = rootPath.resolve(subDir).normalize();
            Files.createDirectories(targetDir);

            String savedFileName = System.currentTimeMillis() + "_" + cleanName;
            Path targetFile = targetDir.resolve(savedFileName);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream();
                 OutputStream out = Files.newOutputStream(targetFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                    out.write(buffer, 0, bytesRead);
                }
            }

            String sha256Hex = HexFormat.of().formatHex(digest.digest());
            String relativeStoragePath = subDir + "/" + savedFileName;

            return StorageUploadResult.builder()
                    .fileName(originalFilename)
                    .storagePath(relativeStoragePath)
                    .storageProvider("LOCAL")
                    .sha256Hash(sha256Hex)
                    .fileSize(file.getSize())
                    .publicOrSignedUrl("/api/evidence/download?path=" + relativeStoragePath)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to store local document", e);
        }
    }

    @Override
    public InputStream downloadDocument(String storagePath) {
        try {
            Path file = rootPath.resolve(storagePath).normalize();
            if (!file.startsWith(rootPath) || !Files.exists(file)) {
                throw new FileNotFoundException("Document not found: " + storagePath);
            }
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read local document: " + storagePath, e);
        }
    }

    @Override
    public String generateAccessUrl(String storagePath, Duration ttl) {
        return "/api/evidence/download?path=" + storagePath;
    }

    @Override
    public void deleteDocument(String storagePath) {
        try {
            Path file = rootPath.resolve(storagePath).normalize();
            if (file.startsWith(rootPath)) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            log.warn("Failed to delete document at {}: {}", storagePath, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "LOCAL";
    }
}
