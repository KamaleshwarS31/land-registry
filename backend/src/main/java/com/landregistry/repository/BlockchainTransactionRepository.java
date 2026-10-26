package com.landregistry.repository;

import com.landregistry.model.BlockchainTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockchainTransactionRepository extends JpaRepository<BlockchainTransaction, String> {
    Optional<BlockchainTransaction> findByTxHash(String txHash);
    Optional<BlockchainTransaction> findByMutationId(String mutationId);
}
