package com.landregistry.controller;

import com.landregistry.model.EvidenceRecord;
import com.landregistry.model.User;
import com.landregistry.repository.UserRepository;
import com.landregistry.service.EvidenceService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;
    private final UserRepository userRepository;

    public EvidenceController(EvidenceService evidenceService, UserRepository userRepository) {
        this.evidenceService = evidenceService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadEvidence(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parcelId", required = false) String parcelId,
            @RequestParam(value = "requestId", required = false) String requestId,
            @RequestParam("sourceId") String sourceId,
            @RequestParam(value = "sourceRecordId", required = false) String sourceRecordId,
            @RequestParam(value = "attributeName", required = false) String attributeName,
            @RequestParam(value = "attributeValue", required = false) String attributeValue,
            @RequestParam(value = "versionLabel", required = false) String versionLabel,
            Authentication authentication
    ) {
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        EvidenceRecord record = evidenceService.uploadEvidence(
                file, parcelId, requestId, sourceId, sourceRecordId,
                attributeName, attributeValue, versionLabel, user
        );

        String accessUrl = evidenceService.getAccessUrl(record);

        return ResponseEntity.ok(Map.of(
                "id", record.getId(),
                "fileName", record.getFileName(),
                "sha256Hash", record.getSha256Hash(),
                "storageProvider", record.getStorageProvider(),
                "accessUrl", accessUrl
        ));
    }

    @GetMapping("/parcel/{parcelId}")
    public ResponseEntity<List<EvidenceRecord>> getEvidenceForParcel(@PathVariable String parcelId) {
        return ResponseEntity.ok(evidenceService.getEvidenceByParcelId(parcelId));
    }

    @GetMapping("/mutation/{mutationId}")
    public ResponseEntity<List<EvidenceRecord>> getEvidenceForMutation(@PathVariable String mutationId) {
        return ResponseEntity.ok(evidenceService.getEvidenceByRequestId(mutationId));
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<?> getSignedUrl(@PathVariable String id) {
        return evidenceService.getEvidenceById(id)
                .map(rec -> ResponseEntity.ok(Map.of("url", evidenceService.getAccessUrl(rec))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("path") String path) {
        try {
            InputStream stream = evidenceService.getDocumentStream(path);
            InputStreamResource resource = new InputStreamResource(stream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"document.bin\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
