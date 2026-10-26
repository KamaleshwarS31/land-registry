package com.landregistry.verification.m2;

public class LineageVerificationResult {
    private boolean valid;
    private String explanation;
    private double areaDiscrepancySqm;

    public LineageVerificationResult() {}

    public LineageVerificationResult(boolean valid, String explanation, double areaDiscrepancySqm) {
        this.valid = valid;
        this.explanation = explanation;
        this.areaDiscrepancySqm = areaDiscrepancySqm;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public double getAreaDiscrepancySqm() { return areaDiscrepancySqm; }
    public void setAreaDiscrepancySqm(double areaDiscrepancySqm) { this.areaDiscrepancySqm = areaDiscrepancySqm; }

    public static class Builder {
        private boolean valid;
        private String explanation;
        private double areaDiscrepancySqm;

        public Builder valid(boolean valid) { this.valid = valid; return this; }
        public Builder explanation(String explanation) { this.explanation = explanation; return this; }
        public Builder areaDiscrepancySqm(double areaDiscrepancySqm) { this.areaDiscrepancySqm = areaDiscrepancySqm; return this; }

        public LineageVerificationResult build() {
            return new LineageVerificationResult(valid, explanation, areaDiscrepancySqm);
        }
    }
}
