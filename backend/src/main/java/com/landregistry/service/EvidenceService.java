package com.landregistry.service;

import com.landregistry.evidence.DocumentStorageService;
import com.landregistry.evidence.StorageUploadResult;
import com.landregistry.model.EvidenceRecord;
import com.landregistry.model.Parcel;
import com.landregistry.model.User;
import com.landregistry.repository.EvidenceRecordRepository;
import com.landregistry.repository.ParcelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EvidenceService {

    private static final Logger log = LoggerFactory.getLogger(EvidenceService.class);

    private final EvidenceRecordRepository evidenceRecordRepository;
    private final ParcelRepository parcelRepository;
    private final DocumentStorageService documentStorageService;
    private final AuditEventService auditEventService;

    public EvidenceService(
            EvidenceRecordRepository evidenceRecordRepository,
            ParcelRepository parcelRepository,
            DocumentStorageService documentStorageService,
            AuditEventService auditEventService
    ) {
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.parcelRepository = parcelRepository;
        this.documentStorageService = documentStorageService;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public EvidenceRecord uploadEvidence(
            MultipartFile file,
            String parcelId,
            String requestId,
            String sourceId,
            String sourceRecordId,
            String attributeName,
            String attributeValue,
            String versionLabel,
            User uploadedBy
    ) {
        Parcel parcel = null;
        String parcelCode = "unassigned";
        if (parcelId != null && !parcelId.isBlank()) {
            parcel = parcelRepository.findById(parcelId).orElse(null);
            if (parcel != null) {
                parcelCode = parcel.getParcelCode();
            }
        }

        StorageUploadResult uploadResult = documentStorageService.uploadDocument(file, parcelCode, sourceId);

        EvidenceRecord record = EvidenceRecord.builder()
                .id(UUID.randomUUID().toString())
                .parcel(parcel)
                .requestId(requestId)
                .sourceId(sourceId)
                .sourceRecordId(sourceRecordId != null ? sourceRecordId : "REC-" + System.currentTimeMillis())
                .attributeName(attributeName != null ? attributeName.toUpperCase() : "GENERAL_DOCUMENT")
                .attributeValue(attributeValue != null ? attributeValue : uploadResult.getFileName())
                .versionLabel(versionLabel != null ? versionLabel : "v1.0")
                .observedAt(Instant.now())
                .fileName(uploadResult.getFileName())
                .filePath(uploadResult.getStoragePath())
                .storageProvider(uploadResult.getStorageProvider())
                .sha256Hash(uploadResult.getSha256Hash())
                .uploadedBy(uploadedBy)
                .createdAt(Instant.now())
                .build();

        EvidenceRecord saved = evidenceRecordRepository.save(record);

        auditEventService.recordEvent(
                uploadedBy.getId(),
                "UPLOAD_EVIDENCE",
                "EVIDENCE",
                saved.getId(),
                String.format("Uploaded document %s (SHA-256: %s, Storage: %s)",
                        uploadResult.getFileName(), uploadResult.getSha256Hash(), uploadResult.getStorageProvider())
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<EvidenceRecord> getEvidenceByParcelId(String parcelId) {
        return evidenceRecordRepository.findByParcelIdOrderByObservedAtDesc(parcelId);
    }

    @Transactional(readOnly = true)
    public List<EvidenceRecord> getEvidenceByRequestId(String requestId) {
        return evidenceRecordRepository.findByRequestIdOrderByObservedAtDesc(requestId);
    }

    @Transactional(readOnly = true)
    public Optional<EvidenceRecord> getEvidenceById(String id) {
        return evidenceRecordRepository.findById(id);
    }

    public String getAccessUrl(EvidenceRecord record) {
        if ("SUPABASE".equalsIgnoreCase(record.getStorageProvider())) {
            return documentStorageService.generateAccessUrl(record.getFilePath(), Duration.ofHours(2));
        }
        return "/api/evidence/download?path=" + record.getFilePath();
    }

    public InputStream getDocumentStream(String filePath) {
        return documentStorageService.downloadDocument(filePath);
    }
}
