package com.landregistry.evidence;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "SUPABASE")
public class SupabaseStorageService implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final String supabaseUrl;
    private final String supabaseKey;
    private final String bucketName;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SupabaseStorageService(
            @Value("${app.supabase.url:https://YOUR_PROJECT_REF.supabase.co}") String supabaseUrl,
            @Value("${app.supabase.key:}") String supabaseKey,
            @Value("${app.supabase.bucket:land-evidence}") String bucketName) {
        this.supabaseUrl = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        this.supabaseKey = supabaseKey;
        this.bucketName = bucketName;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        log.info("Initialized SupabaseStorageService for bucket: {} at {}", bucketName, this.supabaseUrl);
    }

    @Override
    public StorageUploadResult uploadDocument(MultipartFile file, String parcelCode, String sourceId) {
        try {
            String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document.bin";
            String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storagePath = (parcelCode != null ? parcelCode : "unassigned") + "/" + sourceId + "/" + System.currentTimeMillis() + "_" + cleanName;

            byte[] bytes = file.getBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            String sha256Hex = HexFormat.of().formatHex(hash);

            String uploadEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, storagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(bytes, headers);
            ResponseEntity<String> response = restTemplate.exchange(uploadEndpoint, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Supabase Storage upload failed with status " + response.getStatusCode());
            }

            String signedUrl = generateAccessUrl(storagePath, Duration.ofHours(1));

            return StorageUploadResult.builder()
                    .fileName(originalFilename)
                    .storagePath(storagePath)
                    .storageProvider("SUPABASE")
                    .sha256Hash(sha256Hex)
                    .fileSize(file.getSize())
                    .publicOrSignedUrl(signedUrl)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload to Supabase Storage", e);
            throw new RuntimeException("Supabase storage upload error: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream downloadDocument(String storagePath) {
        try {
            String downloadEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, storagePath);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(downloadEndpoint, HttpMethod.GET, requestEntity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return new ByteArrayInputStream(response.getBody());
            }
            throw new RuntimeException("Failed to download file from Supabase Storage: " + response.getStatusCode());
        } catch (Exception e) {
            throw new RuntimeException("Supabase download error: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateAccessUrl(String storagePath, Duration ttl) {
        try {
            long expiresInSeconds = ttl != null ? ttl.getSeconds() : 3600;
            String signEndpoint = String.format("%s/storage/v1/object/sign/%s/%s", supabaseUrl, bucketName, storagePath);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of("expiresIn", expiresInSeconds);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(signEndpoint, HttpMethod.POST, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                if (json.has("signedURL")) {
                    String relativeUrl = json.get("signedURL").asText();
                    return supabaseUrl + "/storage/v1" + relativeUrl;
                }
            }
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, storagePath);
        } catch (Exception e) {
            log.warn("Failed to generate signed URL, falling back to public format: {}", e.getMessage());
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, storagePath);
        }
    }

    @Override
    public void deleteDocument(String storagePath) {
        try {
            String deleteEndpoint = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, storagePath);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            restTemplate.exchange(deleteEndpoint, HttpMethod.DELETE, requestEntity, Void.class);
        } catch (Exception e) {
            log.warn("Supabase delete failed for {}: {}", storagePath, e.getMessage());
        }
    }

    @Override
    public String getProviderName() {
        return "SUPABASE";
    }
}
