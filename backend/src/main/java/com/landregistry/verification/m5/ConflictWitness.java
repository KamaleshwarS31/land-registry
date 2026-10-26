package com.landregistry.verification.m5;

import java.util.List;

public class ConflictWitness {
    private String ruleCode;
    private List<String> sourceIds;
    private List<String> conflictingAssertionIds;
    private String explanation;
    private String severity; // HIGH, MEDIUM, LOW

    public ConflictWitness() {}

    public ConflictWitness(String ruleCode, List<String> sourceIds, List<String> conflictingAssertionIds, String explanation, String severity) {
        this.ruleCode = ruleCode;
        this.sourceIds = sourceIds;
        this.conflictingAssertionIds = conflictingAssertionIds;
        this.explanation = explanation;
        this.severity = severity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public List<String> getSourceIds() { return sourceIds; }
    public void setSourceIds(List<String> sourceIds) { this.sourceIds = sourceIds; }
    public List<String> getConflictingAssertionIds() { return conflictingAssertionIds; }
    public void setConflictingAssertionIds(List<String> conflictingAssertionIds) { this.conflictingAssertionIds = conflictingAssertionIds; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public static class Builder {
        private String ruleCode;
        private List<String> sourceIds;
        private List<String> conflictingAssertionIds;
        private String explanation;
        private String severity;

        public Builder ruleCode(String ruleCode) { this.ruleCode = ruleCode; return this; }
        public Builder sourceIds(List<String> sourceIds) { this.sourceIds = sourceIds; return this; }
        public Builder conflictingAssertionIds(List<String> conflictingAssertionIds) { this.conflictingAssertionIds = conflictingAssertionIds; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder severity(String severity) { this.severity = severity; return this; }

        public ConflictWitness build() {
            return new ConflictWitness(ruleCode, sourceIds, conflictingAssertionIds, explanation, severity);
        }
    }
}
