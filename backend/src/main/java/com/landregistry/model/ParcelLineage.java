package com.landregistry.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "parcel_lineage")
public class ParcelLineage {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_parcel_id", nullable = false)
    private Parcel parentParcel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_parcel_id", nullable = false)
    private Parcel childParcel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutation_id", nullable = false)
    private MutationRequest mutation;

    @Column(name = "relation_type", length = 20, nullable = false)
    private String relationType; // SPLIT, MERGE

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ParcelLineage() {}

    public ParcelLineage(String id, Parcel parentParcel, Parcel childParcel, MutationRequest mutation, String relationType, Instant createdAt) {
        this.id = id;
        this.parentParcel = parentParcel;
        this.childParcel = childParcel;
        this.mutation = mutation;
        this.relationType = relationType;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Parcel getParentParcel() { return parentParcel; }
    public void setParentParcel(Parcel parentParcel) { this.parentParcel = parentParcel; }
    public Parcel getChildParcel() { return childParcel; }
    public void setChildParcel(Parcel childParcel) { this.childParcel = childParcel; }
    public MutationRequest getMutation() { return mutation; }
    public void setMutation(MutationRequest mutation) { this.mutation = mutation; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private Parcel parentParcel;
        private Parcel childParcel;
        private MutationRequest mutation;
        private String relationType;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder parentParcel(Parcel parentParcel) { this.parentParcel = parentParcel; return this; }
        public Builder childParcel(Parcel childParcel) { this.childParcel = childParcel; return this; }
        public Builder mutation(MutationRequest mutation) { this.mutation = mutation; return this; }
        public Builder relationType(String relationType) { this.relationType = relationType; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ParcelLineage build() {
            return new ParcelLineage(id, parentParcel, childParcel, mutation, relationType, createdAt);
        }
    }
}
