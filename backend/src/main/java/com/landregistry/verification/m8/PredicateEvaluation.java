package com.landregistry.verification.m8;

public class PredicateEvaluation {
    private String predicateCode;
    private PredicateStatus status;
    private String explanation;
    private String evidenceRefs;
    private boolean mandatory;

    public PredicateEvaluation() {}

    public PredicateEvaluation(String predicateCode, PredicateStatus status, String explanation, String evidenceRefs, boolean mandatory) {
        this.predicateCode = predicateCode;
        this.status = status;
        this.explanation = explanation;
        this.evidenceRefs = evidenceRefs;
        this.mandatory = mandatory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPredicateCode() { return predicateCode; }
    public void setPredicateCode(String predicateCode) { this.predicateCode = predicateCode; }
    public PredicateStatus getStatus() { return status; }
    public void setStatus(PredicateStatus status) { this.status = status; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getEvidenceRefs() { return evidenceRefs; }
    public void setEvidenceRefs(String evidenceRefs) { this.evidenceRefs = evidenceRefs; }
    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public static class Builder {
        private String predicateCode;
        private PredicateStatus status;
        private String explanation;
        private String evidenceRefs;
        private boolean mandatory;

        public Builder predicateCode(String predicateCode) { this.predicateCode = predicateCode; return this; }
        public Builder status(PredicateStatus status) { this.status = status; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder evidenceRefs(String evidenceRefs) { this.evidenceRefs = evidenceRefs; return this; }
        public Builder mandatory(boolean mandatory) { this.mandatory = mandatory; return this; }

        public PredicateEvaluation build() {
            return new PredicateEvaluation(predicateCode, status, explanation, evidenceRefs, mandatory);
        }
    }
}
