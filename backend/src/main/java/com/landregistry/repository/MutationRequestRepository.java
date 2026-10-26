package com.landregistry.repository;

import com.landregistry.model.MutationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MutationRequestRepository extends JpaRepository<MutationRequest, String> {
    List<MutationRequest> findByRequestedByIdOrderByCreatedAtDesc(String requestedById);
    List<MutationRequest> findByStatusOrderByCreatedAtDesc(String status);
    Page<MutationRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
