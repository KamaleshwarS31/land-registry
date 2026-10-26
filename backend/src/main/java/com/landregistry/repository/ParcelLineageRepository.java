package com.landregistry.repository;

import com.landregistry.model.ParcelLineage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelLineageRepository extends JpaRepository<ParcelLineage, String> {
    List<ParcelLineage> findByParentParcelId(String parentParcelId);
    List<ParcelLineage> findByChildParcelId(String childParcelId);
    List<ParcelLineage> findByMutationId(String mutationId);
}
