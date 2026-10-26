package com.landregistry.repository;

import com.landregistry.model.PredicateResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredicateResultRepository extends JpaRepository<PredicateResult, String> {
    List<PredicateResult> findByMutationIdOrderByEvaluatedAtAsc(String mutationId);
    void deleteByMutationId(String mutationId);
}
