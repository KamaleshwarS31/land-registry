package com.landregistry.blockchain;

public class BlockchainCommitResult {
    private boolean success;
    private String txHash;
    private long blockNumber;
    private long gasUsed;
    private String errorMessage;
    private boolean simulated;

    public BlockchainCommitResult() {}

    public BlockchainCommitResult(boolean success, String txHash, long blockNumber, long gasUsed, String errorMessage, boolean simulated) {
        this.success = success;
        this.txHash = txHash;
        this.blockNumber = blockNumber;
        this.gasUsed = gasUsed;
        this.errorMessage = errorMessage;
        this.simulated = simulated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(long blockNumber) { this.blockNumber = blockNumber; }
    public long getGasUsed() { return gasUsed; }
    public void setGasUsed(long gasUsed) { this.gasUsed = gasUsed; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public boolean isSimulated() { return simulated; }
    public void setSimulated(boolean simulated) { this.simulated = simulated; }

    public static class Builder {
        private boolean success;
        private String txHash;
        private long blockNumber;
        private long gasUsed;
        private String errorMessage;
        private boolean simulated;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder txHash(String txHash) { this.txHash = txHash; return this; }
        public Builder blockNumber(long blockNumber) { this.blockNumber = blockNumber; return this; }
        public Builder gasUsed(long gasUsed) { this.gasUsed = gasUsed; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder simulated(boolean simulated) { this.simulated = simulated; return this; }

        public BlockchainCommitResult build() {
            return new BlockchainCommitResult(success, txHash, blockNumber, gasUsed, errorMessage, simulated);
        }
    }
}
