package com.landregistry.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "decision_certificates")
public class DecisionCertificate {

    @Id
    @Column(length = 36)
    private String id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mutation_id", nullable = false, unique = true)
    private MutationRequest mutation;

    @Column(length = 20, nullable = false)
    private String decision; // AUTHORIZE, HOLD, REJECT

    @Column(name = "canonical_payload", columnDefinition = "TEXT", nullable = false)
    private String canonicalPayload;

    @Column(name = "certificate_hash", length = 64, nullable = false)
    private String certificateHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DecisionCertificate() {}

    public DecisionCertificate(String id, MutationRequest mutation, String decision, String canonicalPayload, String certificateHash, Instant createdAt) {
        this.id = id;
        this.mutation = mutation;
        this.decision = decision;
        this.canonicalPayload = canonicalPayload;
        this.certificateHash = certificateHash;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MutationRequest getMutation() { return mutation; }
    public void setMutation(MutationRequest mutation) { this.mutation = mutation; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getCanonicalPayload() { return canonicalPayload; }
    public void setCanonicalPayload(String canonicalPayload) { this.canonicalPayload = canonicalPayload; }
    public String getCertificateHash() { return certificateHash; }
    public void setCertificateHash(String certificateHash) { this.certificateHash = certificateHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private MutationRequest mutation;
        private String decision;
        private String canonicalPayload;
        private String certificateHash;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder mutation(MutationRequest mutation) { this.mutation = mutation; return this; }
        public Builder decision(String decision) { this.decision = decision; return this; }
        public Builder canonicalPayload(String canonicalPayload) { this.canonicalPayload = canonicalPayload; return this; }
        public Builder certificateHash(String certificateHash) { this.certificateHash = certificateHash; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public DecisionCertificate build() {
            return new DecisionCertificate(id, mutation, decision, canonicalPayload, certificateHash, createdAt);
        }
    }
}
