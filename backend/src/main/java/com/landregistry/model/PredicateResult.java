package com.landregistry.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "predicate_results")
public class PredicateResult {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutation_id", nullable = false)
    private MutationRequest mutation;

    @Column(name = "predicate_code", length = 50, nullable = false)
    private String predicateCode;

    @Column(length = 20, nullable = false)
    private String status; // SATISFIED, VIOLATED, UNRESOLVED

    @Column(columnDefinition = "TEXT", nullable = false)
    private String explanation;

    @Column(name = "evidence_refs", columnDefinition = "TEXT")
    private String evidenceRefs;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    public PredicateResult() {}

    public PredicateResult(String id, MutationRequest mutation, String predicateCode, String status,
                           String explanation, String evidenceRefs, Instant evaluatedAt) {
        this.id = id;
        this.mutation = mutation;
        this.predicateCode = predicateCode;
        this.status = status;
        this.explanation = explanation;
        this.evidenceRefs = evidenceRefs;
        this.evaluatedAt = evaluatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MutationRequest getMutation() { return mutation; }
    public void setMutation(MutationRequest mutation) { this.mutation = mutation; }
    public String getPredicateCode() { return predicateCode; }
    public void setPredicateCode(String predicateCode) { this.predicateCode = predicateCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getEvidenceRefs() { return evidenceRefs; }
    public void setEvidenceRefs(String evidenceRefs) { this.evidenceRefs = evidenceRefs; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public static class Builder {
        private String id;
        private MutationRequest mutation;
        private String predicateCode;
        private String status;
        private String explanation;
        private String evidenceRefs;
        private Instant evaluatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder mutation(MutationRequest mutation) { this.mutation = mutation; return this; }
        public Builder predicateCode(String predicateCode) { this.predicateCode = predicateCode; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder evidenceRefs(String evidenceRefs) { this.evidenceRefs = evidenceRefs; return this; }
        public Builder evaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; return this; }

        public PredicateResult build() {
            return new PredicateResult(id, mutation, predicateCode, status, explanation, evidenceRefs, evaluatedAt);
        }
    }
}
