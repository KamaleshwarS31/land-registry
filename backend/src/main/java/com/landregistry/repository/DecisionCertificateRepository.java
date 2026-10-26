package com.landregistry.repository;

import com.landregistry.model.DecisionCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecisionCertificateRepository extends JpaRepository<DecisionCertificate, String> {
    Optional<DecisionCertificate> findByMutationId(String mutationId);
}
