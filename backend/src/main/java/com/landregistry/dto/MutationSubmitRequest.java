package com.landregistry.dto;

import java.util.List;
import java.util.Map;

public class MutationSubmitRequest {
    private String mutationType; // REGISTRATION, TRANSFER, SPLIT, MERGE
    private List<String> targetParcelIds;
    private Map<String, Integer> expectedVersions;
    private Map<String, Object> proposedPayload;

    public MutationSubmitRequest() {}

    public MutationSubmitRequest(String mutationType, List<String> targetParcelIds,
                                 Map<String, Integer> expectedVersions, Map<String, Object> proposedPayload) {
        this.mutationType = mutationType;
        this.targetParcelIds = targetParcelIds;
        this.expectedVersions = expectedVersions;
        this.proposedPayload = proposedPayload;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getMutationType() { return mutationType; }
    public void setMutationType(String mutationType) { this.mutationType = mutationType; }
    public List<String> getTargetParcelIds() { return targetParcelIds; }
    public void setTargetParcelIds(List<String> targetParcelIds) { this.targetParcelIds = targetParcelIds; }
    public Map<String, Integer> getExpectedVersions() { return expectedVersions; }
    public void setExpectedVersions(Map<String, Integer> expectedVersions) { this.expectedVersions = expectedVersions; }
    public Map<String, Object> getProposedPayload() { return proposedPayload; }
    public void setProposedPayload(Map<String, Object> proposedPayload) { this.proposedPayload = proposedPayload; }

    public static class Builder {
        private String mutationType;
        private List<String> targetParcelIds;
        private Map<String, Integer> expectedVersions;
        private Map<String, Object> proposedPayload;

        public Builder mutationType(String mutationType) { this.mutationType = mutationType; return this; }
        public Builder targetParcelIds(List<String> targetParcelIds) { this.targetParcelIds = targetParcelIds; return this; }
        public Builder expectedVersions(Map<String, Integer> expectedVersions) { this.expectedVersions = expectedVersions; return this; }
        public Builder proposedPayload(Map<String, Object> proposedPayload) { this.proposedPayload = proposedPayload; return this; }

        public MutationSubmitRequest build() {
            return new MutationSubmitRequest(mutationType, targetParcelIds, expectedVersions, proposedPayload);
        }
    }
}
