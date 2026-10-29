package com.landregistry.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "evidence_records")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvidenceRecord {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcel_id")
    private Parcel parcel;

    @Column(name = "request_id", length = 36)
    private String requestId;

    @Column(name = "source_id", length = 50, nullable = false)
    private String sourceId; // REGISTRATION_DEED, CADASTRAL_SURVEY, REVENUE_RECORD, COURT_ORDER, MORTGAGE_REGISTRY

    @Column(name = "source_record_id", length = 100, nullable = false)
    private String sourceRecordId;

    @Column(name = "attribute_name", length = 50, nullable = false)
    private String attributeName;

    @Column(name = "attribute_value", columnDefinition = "TEXT", nullable = false)
    private String attributeValue;

    @Column(name = "version_label", length = 30, nullable = false)
    private String versionLabel;

    @Column(name = "observed_at", nullable = false)
    private Instant observedAt;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "storage_provider", length = 30, nullable = false)
    private String storageProvider; // LOCAL or SUPABASE

    @Column(name = "sha256_hash", length = 64, nullable = false)
    private String sha256Hash;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User uploadedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public EvidenceRecord() {}

    public EvidenceRecord(String id, Parcel parcel, String requestId, String sourceId, String sourceRecordId,
                          String attributeName, String attributeValue, String versionLabel, Instant observedAt,
                          String fileName, String filePath, String storageProvider, String sha256Hash,
                          User uploadedBy, Instant createdAt) {
        this.id = id;
        this.parcel = parcel;
        this.requestId = requestId;
        this.sourceId = sourceId;
        this.sourceRecordId = sourceRecordId;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
        this.versionLabel = versionLabel;
        this.observedAt = observedAt;
        this.fileName = fileName;
        this.filePath = filePath;
        this.storageProvider = storageProvider;
        this.sha256Hash = sha256Hash;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    @JsonIgnore
    public Parcel getParcel() { return parcel; }
    public void setParcel(Parcel parcel) { this.parcel = parcel; }

    @JsonProperty("parcelId")
    public String getParcelId() {
        return parcel != null ? parcel.getId() : null;
    }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getAttributeValue() { return attributeValue; }
    public void setAttributeValue(String attributeValue) { this.attributeValue = attributeValue; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }
    public String getSha256Hash() { return sha256Hash; }
    public void setSha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; }
    public User getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private Parcel parcel;
        private String requestId;
        private String sourceId;
        private String sourceRecordId;
        private String attributeName;
        private String attributeValue;
        private String versionLabel;
        private Instant observedAt;
        private String fileName;
        private String filePath;
        private String storageProvider = "LOCAL";
        private String sha256Hash;
        private User uploadedBy;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder parcel(Parcel parcel) { this.parcel = parcel; return this; }
        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder sourceId(String sourceId) { this.sourceId = sourceId; return this; }
        public Builder sourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; return this; }
        public Builder attributeName(String attributeName) { this.attributeName = attributeName; return this; }
        public Builder attributeValue(String attributeValue) { this.attributeValue = attributeValue; return this; }
        public Builder versionLabel(String versionLabel) { this.versionLabel = versionLabel; return this; }
        public Builder observedAt(Instant observedAt) { this.observedAt = observedAt; return this; }
        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder filePath(String filePath) { this.filePath = filePath; return this; }
        public Builder storageProvider(String storageProvider) { this.storageProvider = storageProvider; return this; }
        public Builder sha256Hash(String sha256Hash) { this.sha256Hash = sha256Hash; return this; }
        public Builder uploadedBy(User uploadedBy) { this.uploadedBy = uploadedBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public EvidenceRecord build() {
            return new EvidenceRecord(id, parcel, requestId, sourceId, sourceRecordId, attributeName, attributeValue, versionLabel, observedAt, fileName, filePath, storageProvider, sha256Hash, uploadedBy, createdAt);
        }
    }
}
