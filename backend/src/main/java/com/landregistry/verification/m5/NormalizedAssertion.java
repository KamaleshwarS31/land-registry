package com.landregistry.verification.m5;

import java.time.Instant;

public class NormalizedAssertion {
    private String evidenceId;
    private String sourceId;
    private String sourceRecordId;
    private String parcelCode;
    private String attributeName;
    private String rawValue;
    private String normalizedValue;
    private String versionLabel;
    private Instant observedAt;

    public NormalizedAssertion() {}

    public NormalizedAssertion(String evidenceId, String sourceId, String sourceRecordId, String parcelCode,
                               String attributeName, String rawValue, String normalizedValue, String versionLabel, Instant observedAt) {
        this.evidenceId = evidenceId;
        this.sourceId = sourceId;
        this.sourceRecordId = sourceRecordId;
        this.parcelCode = parcelCode;
        this.attributeName = attributeName;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.versionLabel = versionLabel;
        this.observedAt = observedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getEvidenceId() { return evidenceId; }
    public void setEvidenceId(String evidenceId) { this.evidenceId = evidenceId; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }
    public String getParcelCode() { return parcelCode; }
    public void setParcelCode(String parcelCode) { this.parcelCode = parcelCode; }
    public String getAttributeName() { return attributeName; }
    public void setAttributeName(String attributeName) { this.attributeName = attributeName; }
    public String getRawValue() { return rawValue; }
    public void setRawValue(String rawValue) { this.rawValue = rawValue; }
    public String getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }
    public Instant getObservedAt() { return observedAt; }
    public void setObservedAt(Instant observedAt) { this.observedAt = observedAt; }

    public static class Builder {
        private String evidenceId;
        private String sourceId;
        private String sourceRecordId;
        private String parcelCode;
        private String attributeName;
        private String rawValue;
        private String normalizedValue;
        private String versionLabel;
        private Instant observedAt;

        public Builder evidenceId(String evidenceId) { this.evidenceId = evidenceId; return this; }
        public Builder sourceId(String sourceId) { this.sourceId = sourceId; return this; }
        public Builder sourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; return this; }
        public Builder parcelCode(String parcelCode) { this.parcelCode = parcelCode; return this; }
        public Builder attributeName(String attributeName) { this.attributeName = attributeName; return this; }
        public Builder rawValue(String rawValue) { this.rawValue = rawValue; return this; }
        public Builder normalizedValue(String normalizedValue) { this.normalizedValue = normalizedValue; return this; }
        public Builder versionLabel(String versionLabel) { this.versionLabel = versionLabel; return this; }
        public Builder observedAt(Instant observedAt) { this.observedAt = observedAt; return this; }

        public NormalizedAssertion build() {
            return new NormalizedAssertion(evidenceId, sourceId, sourceRecordId, parcelCode, attributeName, rawValue, normalizedValue, versionLabel, observedAt);
        }
    }
}
