package com.landregistry.repository;

import com.landregistry.model.Parcel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, String> {
    Optional<Parcel> findByParcelCode(String parcelCode);
    boolean existsByParcelCode(String parcelCode);

    List<Parcel> findByOwnerId(String ownerId);

    @Query("SELECT p FROM Parcel p WHERE " +
           "(:query IS NULL OR LOWER(p.parcelCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.locality) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.surveyNumber) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<Parcel> searchParcels(@Param("query") String query, @Param("status") String status, Pageable pageable);
}
