package com.landregistry.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blockchain_transactions")
public class BlockchainTransaction {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mutation_id", nullable = false)
    private MutationRequest mutation;

    @Column(name = "tx_hash", length = 66, nullable = false, unique = true)
    private String txHash;

    @Column(name = "chain_id", nullable = false)
    private long chainId;

    @Column(name = "contract_address", length = 42, nullable = false)
    private String contractAddress;

    @Column(name = "block_number")
    private Long blockNumber;

    @Column(name = "receipt_status")
    private Integer receiptStatus; // 1 = SUCCESS, 0 = REVERT

    @Column(name = "gas_used")
    private Long gasUsed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public BlockchainTransaction() {}

    public BlockchainTransaction(String id, MutationRequest mutation, String txHash, long chainId,
                                 String contractAddress, Long blockNumber, Integer receiptStatus,
                                 Long gasUsed, Instant createdAt) {
        this.id = id;
        this.mutation = mutation;
        this.txHash = txHash;
        this.chainId = chainId;
        this.contractAddress = contractAddress;
        this.blockNumber = blockNumber;
        this.receiptStatus = receiptStatus;
        this.gasUsed = gasUsed;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MutationRequest getMutation() { return mutation; }
    public void setMutation(MutationRequest mutation) { this.mutation = mutation; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public long getChainId() { return chainId; }
    public void setChainId(long chainId) { this.chainId = chainId; }
    public String getContractAddress() { return contractAddress; }
    public void setContractAddress(String contractAddress) { this.contractAddress = contractAddress; }
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    public Integer getReceiptStatus() { return receiptStatus; }
    public void setReceiptStatus(Integer receiptStatus) { this.receiptStatus = receiptStatus; }
    public Long getGasUsed() { return gasUsed; }
    public void setGasUsed(Long gasUsed) { this.gasUsed = gasUsed; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private MutationRequest mutation;
        private String txHash;
        private long chainId;
        private String contractAddress;
        private Long blockNumber;
        private Integer receiptStatus;
        private Long gasUsed;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder mutation(MutationRequest mutation) { this.mutation = mutation; return this; }
        public Builder txHash(String txHash) { this.txHash = txHash; return this; }
        public Builder chainId(long chainId) { this.chainId = chainId; return this; }
        public Builder contractAddress(String contractAddress) { this.contractAddress = contractAddress; return this; }
        public Builder blockNumber(Long blockNumber) { this.blockNumber = blockNumber; return this; }
        public Builder receiptStatus(Integer receiptStatus) { this.receiptStatus = receiptStatus; return this; }
        public Builder gasUsed(Long gasUsed) { this.gasUsed = gasUsed; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public BlockchainTransaction build() {
            return new BlockchainTransaction(id, mutation, txHash, chainId, contractAddress, blockNumber, receiptStatus, gasUsed, createdAt);
        }
    }
}
