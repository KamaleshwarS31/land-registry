package com.landregistry.dto;

public class MutationReviewRequest {
    private String action; // APPROVE, HOLD, REJECT
    private String reason;

    public MutationReviewRequest() {}

    public MutationReviewRequest(String action, String reason) {
        this.action = action;
        this.reason = reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public static class Builder {
        private String action;
        private String reason;

        public Builder action(String action) { this.action = action; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }

        public MutationReviewRequest build() {
            return new MutationReviewRequest(action, reason);
        }
    }
}
