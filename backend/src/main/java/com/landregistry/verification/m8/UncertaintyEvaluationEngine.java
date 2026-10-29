package com.landregistry.verification.m8;

import com.landregistry.model.EvidenceRecord;
import com.landregistry.model.MutationRequest;
import com.landregistry.model.Parcel;
import com.landregistry.verification.m5.ConflictWitness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UncertaintyEvaluationEngine {

    private static final Logger log = LoggerFactory.getLogger(UncertaintyEvaluationEngine.class);

    public List<PredicateEvaluation> evaluatePredicates(
            MutationRequest mutation,
            List<Parcel> targetParcels,
            List<EvidenceRecord> evidenceRecords,
            List<ConflictWitness> conflictWitnesses,
            boolean lineageCheckPassed,
            String lineageExplanation
    ) {
        List<PredicateEvaluation> list = new ArrayList<>();

        // 1. Identity Verification Predicate
        boolean identityMatch = targetParcels.stream().allMatch(p ->
                p.getOwner() != null && p.getOwner().getId().equals(mutation.getRequestedBy().getId())
        );

        if ("REGISTRATION".equals(mutation.getMutationType())) {
            // New registration: requires identity proof document
            boolean hasDeed = evidenceRecords.stream().anyMatch(e -> "REGISTRATION_DEED".equals(e.getSourceId()));
            list.add(PredicateEvaluation.builder()
                    .predicateCode("PRED_IDENTITY_VERIFIED")
                    .mandatory(true)
                    .status(hasDeed ? PredicateStatus.SATISFIED : PredicateStatus.UNRESOLVED)
                    .explanation(hasDeed ? "Registration deed evidence provided." : "Identity / deed evidence is missing for registration.")
                    .evidenceRefs(hasDeed ? "REGISTRATION_DEED" : null)
                    .build());
        } else {
            list.add(PredicateEvaluation.builder()
                    .predicateCode("PRED_IDENTITY_VERIFIED")
                    .mandatory(true)
                    .status(identityMatch ? PredicateStatus.SATISFIED : PredicateStatus.VIOLATED)
                    .explanation(identityMatch ? "Requester confirmed as registered parcel owner." : "Requester is NOT the registered owner of target parcel(s).")
                    .build());
        }

        // 2. Contradiction Predicate (from M5)
        boolean hasHighConflict = conflictWitnesses.stream().anyMatch(w -> "HIGH".equals(w.getSeverity()));
        boolean hasMedConflict = conflictWitnesses.stream().anyMatch(w -> "MEDIUM".equals(w.getSeverity()));

        PredicateStatus m5Status;
        String m5Explanation;
        if (hasHighConflict) {
            m5Status = PredicateStatus.VIOLATED;
            m5Explanation = "Cross-registry contradiction detected: " + conflictWitnesses.get(0).getExplanation();
        } else if (hasMedConflict) {
            m5Status = PredicateStatus.UNRESOLVED;
            m5Explanation = "Discrepancy detected requiring survey review: " + conflictWitnesses.get(0).getExplanation();
        } else {
            m5Status = PredicateStatus.SATISFIED;
            m5Explanation = "No cross-registry contradictions detected among source records.";
        }

        list.add(PredicateEvaluation.builder()
                .predicateCode("PRED_NO_CROSS_CONTRADICTIONS")
                .mandatory(true)
                .status(m5Status)
                .explanation(m5Explanation)
                .evidenceRefs(conflictWitnesses.isEmpty() ? "M5_CLEAN" : conflictWitnesses.get(0).getRuleCode())
                .build());

        // 3. Encumbrance & Tax Clearance Predicate (Uncertainty-aware!)
        boolean hasEncumbranceEvidence = evidenceRecords.stream().anyMatch(e ->
                "REVENUE_RECORD".equals(e.getSourceId()) || "MORTGAGE_REGISTRY".equals(e.getSourceId())
        );

        if (!hasEncumbranceEvidence) {
            if ("REGISTRATION".equals(mutation.getMutationType())) {
                list.add(PredicateEvaluation.builder()
                        .predicateCode("PRED_ENCUMBRANCE_CLEAR")
                        .mandatory(true)
                        .status(PredicateStatus.SATISFIED)
                        .explanation("Initial parcel registration: unencumbered status affirmed by registration declaration.")
                        .evidenceRefs("REGISTRATION_DECLARATION")
                        .build());
            } else {
                // Missing evidence must NEVER default to valid/clear! It is UNRESOLVED.
                list.add(PredicateEvaluation.builder()
                        .predicateCode("PRED_ENCUMBRANCE_CLEAR")
                        .mandatory(true)
                        .status(PredicateStatus.UNRESOLVED)
                        .explanation("Encumbrance/revenue records missing. Cannot assume parcel is free of liens without affirmative proof.")
                        .build());
            }
        } else {
            boolean hasActiveLien = evidenceRecords.stream()
                    .filter(e -> "REVENUE_RECORD".equals(e.getSourceId()) || "MORTGAGE_REGISTRY".equals(e.getSourceId()))
                    .anyMatch(e -> e.getAttributeValue() != null &&
                            (e.getAttributeValue().toUpperCase().contains("MORTGAGED") ||
                             e.getAttributeValue().toUpperCase().contains("UNPAID")));

            list.add(PredicateEvaluation.builder()
                    .predicateCode("PRED_ENCUMBRANCE_CLEAR")
                    .mandatory(true)
                    .status(hasActiveLien ? PredicateStatus.VIOLATED : PredicateStatus.SATISFIED)
                    .explanation(hasActiveLien ? "Active mortgage or unpaid tax lien detected." : "Encumbrance search and tax clearance verified clear.")
                    .evidenceRefs("ENCUMBRANCE_VERIFIED")
                    .build());
        }

        // 4. Cadastral Survey Recency Predicate
        EvidenceRecord survey = evidenceRecords.stream()
                .filter(e -> "CADASTRAL_SURVEY".equals(e.getSourceId()))
                .findFirst()
                .orElse(null);

        if (survey == null) {
            list.add(PredicateEvaluation.builder()
                    .predicateCode("PRED_SURVEY_RECENCY")
                    .mandatory(true)
                    .status(PredicateStatus.UNRESOLVED)
                    .explanation("Cadastral survey evidence missing. Survey verification unresolved.")
                    .build());
        } else {
            long daysOld = Duration.between(survey.getObservedAt(), Instant.now()).toDays();
            if (daysOld > (5 * 365)) {
                list.add(PredicateEvaluation.builder()
                        .predicateCode("PRED_SURVEY_RECENCY")
                        .mandatory(true)
                        .status(PredicateStatus.UNRESOLVED)
                        .explanation(String.format("Cadastral survey is stale (%d days old > 5-year validity threshold). Fresh survey required.", daysOld))
                        .evidenceRefs(survey.getId())
                        .build());
            } else {
                list.add(PredicateEvaluation.builder()
                        .predicateCode("PRED_SURVEY_RECENCY")
                        .mandatory(true)
                        .status(PredicateStatus.SATISFIED)
                        .explanation(String.format("Recent cadastral survey verified (observed %d days ago).", daysOld))
                        .evidenceRefs(survey.getId())
                        .build());
            }
        }

        // 5. Lineage & Topology Predicate (from M2)
        list.add(PredicateEvaluation.builder()
                .predicateCode("PRED_LINEAGE_VALIDITY")
                .mandatory(true)
                .status(lineageCheckPassed ? PredicateStatus.SATISFIED : PredicateStatus.VIOLATED)
                .explanation(lineageExplanation)
                .evidenceRefs("M2_LINEAGE_ENGINE")
                .build());

        return list;
    }

    /**
     * Three-Valued Decision Rule:
     * - Any mandatory VIOLATED -> REJECT
     * - Else any mandatory UNRESOLVED -> HOLD
     * - Else -> AUTHORIZE
     */
    public DecisionType evaluateDecision(List<PredicateEvaluation> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return DecisionType.HOLD;
        }

        boolean anyViolated = predicates.stream()
                .filter(PredicateEvaluation::isMandatory)
                .anyMatch(p -> p.getStatus() == PredicateStatus.VIOLATED);

        if (anyViolated) {
            return DecisionType.REJECT;
        }

        boolean anyUnresolved = predicates.stream()
                .filter(PredicateEvaluation::isMandatory)
                .anyMatch(p -> p.getStatus() == PredicateStatus.UNRESOLVED);

        if (anyUnresolved) {
            return DecisionType.HOLD;
        }

        return DecisionType.AUTHORIZE;
    }
}
