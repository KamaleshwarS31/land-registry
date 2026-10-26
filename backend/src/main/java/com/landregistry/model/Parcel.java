package com.landregistry.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "parcels")
public class Parcel {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "parcel_code", length = 50, nullable = false, unique = true)
    private String parcelCode;

    @Column(name = "survey_number", length = 50, nullable = false)
    private String surveyNumber;

    @Column(length = 100, nullable = false)
    private String locality;

    @Column(name = "area_sqm", precision = 15, scale = 4, nullable = false)
    private BigDecimal areaSqm;

    @Column(name = "geometry_wkt", columnDefinition = "TEXT", nullable = false)
    private String geometryWkt;

    @Column(length = 30, nullable = false)
    private String status; // PENDING, ACTIVE, INACTIVE_SPLIT, INACTIVE_MERGED, DISPUTED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private int version;

    @Column(name = "blockchain_tx_hash", length = 66)
    private String blockchainTxHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Parcel() {}

    public Parcel(String id, String parcelCode, String surveyNumber, String locality, BigDecimal areaSqm,
                  String geometryWkt, String status, User owner, int version, String blockchainTxHash,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.parcelCode = parcelCode;
        this.surveyNumber = surveyNumber;
        this.locality = locality;
        this.areaSqm = areaSqm;
        this.geometryWkt = geometryWkt;
        this.status = status;
        this.owner = owner;
        this.version = version;
        this.blockchainTxHash = blockchainTxHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getParcelCode() { return parcelCode; }
    public void setParcelCode(String parcelCode) { this.parcelCode = parcelCode; }
    public String getSurveyNumber() { return surveyNumber; }
    public void setSurveyNumber(String surveyNumber) { this.surveyNumber = surveyNumber; }
    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }
    public BigDecimal getAreaSqm() { return areaSqm; }
    public void setAreaSqm(BigDecimal areaSqm) { this.areaSqm = areaSqm; }
    public String getGeometryWkt() { return geometryWkt; }
    public void setGeometryWkt(String geometryWkt) { this.geometryWkt = geometryWkt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getBlockchainTxHash() { return blockchainTxHash; }
    public void setBlockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private String id;
        private String parcelCode;
        private String surveyNumber;
        private String locality;
        private BigDecimal areaSqm;
        private String geometryWkt;
        private String status;
        private User owner;
        private int version = 1;
        private String blockchainTxHash;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder parcelCode(String parcelCode) { this.parcelCode = parcelCode; return this; }
        public Builder surveyNumber(String surveyNumber) { this.surveyNumber = surveyNumber; return this; }
        public Builder locality(String locality) { this.locality = locality; return this; }
        public Builder areaSqm(BigDecimal areaSqm) { this.areaSqm = areaSqm; return this; }
        public Builder geometryWkt(String geometryWkt) { this.geometryWkt = geometryWkt; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder owner(User owner) { this.owner = owner; return this; }
        public Builder version(int version) { this.version = version; return this; }
        public Builder blockchainTxHash(String blockchainTxHash) { this.blockchainTxHash = blockchainTxHash; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Parcel build() {
            return new Parcel(id, parcelCode, surveyNumber, locality, areaSqm, geometryWkt, status, owner, version, blockchainTxHash, createdAt, updatedAt);
        }
    }
}
