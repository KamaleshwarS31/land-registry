package com.landregistry.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mutation_requests")
public class MutationRequest {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "mutation_type", length = 30, nullable = false)
    private String mutationType; // REGISTRATION, TRANSFER, SPLIT, MERGE

    @Column(name = "target_parcel_ids", columnDefinition = "TEXT", nullable = false)
    private String targetParcelIds; // JSON Array of parcel IDs

    @Column(name = "expected_versions", columnDefinition = "TEXT", nullable = false)
    private String expectedVersions; // JSON map: parcelId -> version

    @Column(name = "proposed_payload", columnDefinition = "TEXT", nullable = false)
    private String proposedPayload; // JSON containing buyer ID, child geometries, etc.

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_reviewer")
    private User assignedReviewer;

    @Column(length = 30, nullable = false)
    private String status; // SUBMITTED, EVALUATING, HELD, REJECTED, READY_FOR_COMMIT, COMMITTED, COMMIT_FAILED

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public MutationRequest() {}

    public MutationRequest(String id, String mutationType, String targetParcelIds, String expectedVersions,
                           String proposedPayload, User requestedBy, User assignedReviewer, String status,
                           String decisionReason, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.mutationType = mutationType;
        this.targetParcelIds = targetParcelIds;
        this.expectedVersions = expectedVersions;
        this.proposedPayload = proposedPayload;
        this.requestedBy = requestedBy;
        this.assignedReviewer = assignedReviewer;
        this.status = status;
        this.decisionReason = decisionReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMutationType() { return mutationType; }
    public void setMutationType(String mutationType) { this.mutationType = mutationType; }
    public String getTargetParcelIds() { return targetParcelIds; }
    public void setTargetParcelIds(String targetParcelIds) { this.targetParcelIds = targetParcelIds; }
    public String getExpectedVersions() { return expectedVersions; }
    public void setExpectedVersions(String expectedVersions) { this.expectedVersions = expectedVersions; }
    public String getProposedPayload() { return proposedPayload; }
    public void setProposedPayload(String proposedPayload) { this.proposedPayload = proposedPayload; }
    public User getRequestedBy() { return requestedBy; }
    public void setRequestedBy(User requestedBy) { this.requestedBy = requestedBy; }
    public User getAssignedReviewer() { return assignedReviewer; }
    public void setAssignedReviewer(User assignedReviewer) { this.assignedReviewer = assignedReviewer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private String id;
        private String mutationType;
        private String targetParcelIds;
        private String expectedVersions;
        private String proposedPayload;
        private User requestedBy;
        private User assignedReviewer;
        private String status = "SUBMITTED";
        private String decisionReason;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder mutationType(String mutationType) { this.mutationType = mutationType; return this; }
        public Builder targetParcelIds(String targetParcelIds) { this.targetParcelIds = targetParcelIds; return this; }
        public Builder expectedVersions(String expectedVersions) { this.expectedVersions = expectedVersions; return this; }
        public Builder proposedPayload(String proposedPayload) { this.proposedPayload = proposedPayload; return this; }
        public Builder requestedBy(User requestedBy) { this.requestedBy = requestedBy; return this; }
        public Builder assignedReviewer(User assignedReviewer) { this.assignedReviewer = assignedReviewer; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder decisionReason(String decisionReason) { this.decisionReason = decisionReason; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public MutationRequest build() {
            return new MutationRequest(id, mutationType, targetParcelIds, expectedVersions, proposedPayload, requestedBy, assignedReviewer, status, decisionReason, createdAt, updatedAt);
        }
    }
}
