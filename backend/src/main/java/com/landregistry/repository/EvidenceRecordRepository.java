package com.landregistry.repository;

import com.landregistry.model.EvidenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvidenceRecordRepository extends JpaRepository<EvidenceRecord, String> {
    List<EvidenceRecord> findByParcelIdOrderByObservedAtDesc(String parcelId);
    List<EvidenceRecord> findByRequestIdOrderByObservedAtDesc(String requestId);
    List<EvidenceRecord> findByParcelParcelCodeOrderByObservedAtDesc(String parcelCode);
}
