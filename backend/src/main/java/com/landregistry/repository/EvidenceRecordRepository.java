package com.landregistry.repository;

import com.landregistry.model.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRecordRepository extends JpaRepository<EvidenceRecord, String> {
    @Query("SELECT e FROM EvidenceRecord e WHERE e.parcel.id = :parcelId ORDER BY e.observedAt DESC")
    List<EvidenceRecord> findByParcelIdOrderByObservedAtDesc(@Param("parcelId") String parcelId);

    List<EvidenceRecord> findByRequestIdOrderByObservedAtDesc(String requestId);

    @Query("SELECT e FROM EvidenceRecord e WHERE e.parcel.parcelCode = :parcelCode ORDER BY e.observedAt DESC")
    List<EvidenceRecord> findByParcelParcelCodeOrderByObservedAtDesc(@Param("parcelCode") String parcelCode);
}
