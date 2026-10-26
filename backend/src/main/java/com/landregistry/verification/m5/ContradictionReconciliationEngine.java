package com.landregistry.verification.m5;

import com.landregistry.model.EvidenceRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContradictionReconciliationEngine {

    private static final Logger log = LoggerFactory.getLogger(ContradictionReconciliationEngine.class);

    public List<NormalizedAssertion> normalizeAssertions(List<EvidenceRecord> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        return records.stream().map(rec -> {
            String rawAttr = rec.getAttributeName() != null ? rec.getAttributeName().trim().toUpperCase() : "";
            String rawVal = rec.getAttributeValue() != null ? rec.getAttributeValue().trim() : "";
            String normVal = normalizeValue(rawAttr, rawVal);

            return NormalizedAssertion.builder()
                    .evidenceId(rec.getId())
                    .sourceId(rec.getSourceId())
                    .sourceRecordId(rec.getSourceRecordId())
                    .parcelCode(rec.getParcel() != null ? rec.getParcel().getParcelCode() : null)
                    .attributeName(rawAttr)
                    .rawValue(rawVal)
                    .normalizedValue(normVal)
                    .versionLabel(rec.getVersionLabel())
                    .observedAt(rec.getObservedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    private String normalizeValue(String attr, String value) {
        if (value == null) return "";
        if ("OWNER_NAME".equals(attr)) {
            // Strip honorifics, punctuation, and extra whitespace
            return value.replaceAll("(?i)\\b(mr|mrs|ms|dr|shri|smt)\\b\\.?", "")
                    .replaceAll("[^a-zA-Z0-9 ]", "")
                    .trim()
                    .toLowerCase();
        }
        if ("SURVEY_AREA_SQM".equals(attr) || "AREA".equals(attr)) {
            try {
                BigDecimal bd = new BigDecimal(value.replaceAll("[^0-9.]", ""));
                return bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
            } catch (Exception e) {
                return value.trim();
            }
        }
        return value.trim().toUpperCase();
    }

    public List<ConflictWitness> detectContradictions(List<NormalizedAssertion> assertions) {
        List<ConflictWitness> witnesses = new ArrayList<>();
        if (assertions == null || assertions.isEmpty()) {
            return witnesses;
        }

        // Rule 1: RULE_OWNERSHIP_COLLISION
        Map<String, List<NormalizedAssertion>> ownerClaimsBySource = assertions.stream()
                .filter(a -> "OWNER_NAME".equals(a.getAttributeName()) && !a.getNormalizedValue().isEmpty())
                .collect(Collectors.groupingBy(NormalizedAssertion::getSourceId));

        Set<String> distinctOwners = assertions.stream()
                .filter(a -> "OWNER_NAME".equals(a.getAttributeName()) && !a.getNormalizedValue().isEmpty())
                .map(NormalizedAssertion::getNormalizedValue)
                .collect(Collectors.toSet());

        if (distinctOwners.size() > 1 && ownerClaimsBySource.size() > 1) {
            List<String> sources = new ArrayList<>(ownerClaimsBySource.keySet());
            List<String> evidenceIds = assertions.stream()
                    .filter(a -> "OWNER_NAME".equals(a.getAttributeName()))
                    .map(NormalizedAssertion::getEvidenceId)
                    .collect(Collectors.toList());

            witnesses.add(ConflictWitness.builder()
                    .ruleCode("RULE_OWNERSHIP_COLLISION")
                    .sourceIds(sources)
                    .conflictingAssertionIds(evidenceIds)
                    .severity("HIGH")
                    .explanation(String.format("Cross-registry owner contradiction detected: multiple distinct owners claimed across sources (%s).",
                            String.join(", ", distinctOwners)))
                    .build());
        }

        // Rule 2: RULE_ACTIVE_ENCUMBRANCE
        List<NormalizedAssertion> encumbrances = assertions.stream()
                .filter(a -> "ENCUMBRANCE_STATUS".equals(a.getAttributeName()) ||
                             "TAX_STATUS".equals(a.getAttributeName()) ||
                             "COURT_INJUNCTION".equals(a.getAttributeName()))
                .filter(a -> {
                    String val = a.getNormalizedValue();
                    return val.contains("MORTGAGE") || val.contains("LIEN") ||
                           val.contains("DISPUTE") || val.contains("INJUNCTION") ||
                           val.contains("UNPAID") || val.contains("FROZEN");
                })
                .toList();

        for (NormalizedAssertion enc : encumbrances) {
            witnesses.add(ConflictWitness.builder()
                    .ruleCode("RULE_ACTIVE_ENCUMBRANCE")
                    .sourceIds(List.of(enc.getSourceId()))
                    .conflictingAssertionIds(List.of(enc.getEvidenceId()))
                    .severity("HIGH")
                    .explanation(String.format("Active encumbrance or restriction detected from source [%s]: %s = '%s'.",
                            enc.getSourceId(), enc.getAttributeName(), enc.getRawValue()))
                    .build());
        }

        // Rule 3: RULE_SURVEY_BOUNDARY_DEVIATION
        List<NormalizedAssertion> areaAssertions = assertions.stream()
                .filter(a -> "SURVEY_AREA_SQM".equals(a.getAttributeName()) || "AREA".equals(a.getAttributeName()))
                .toList();

        if (areaAssertions.size() >= 2) {
            List<BigDecimal> areas = new ArrayList<>();
            for (NormalizedAssertion a : areaAssertions) {
                try {
                    areas.add(new BigDecimal(a.getNormalizedValue()));
                } catch (Exception ignored) {}
            }
            if (areas.size() >= 2) {
                BigDecimal min = areas.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal max = areas.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                if (min.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = max.subtract(min);
                    BigDecimal pct = diff.divide(min, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                    if (pct.compareTo(BigDecimal.valueOf(1.5)) > 0) {
                        witnesses.add(ConflictWitness.builder()
                                .ruleCode("RULE_SURVEY_BOUNDARY_DEVIATION")
                                .sourceIds(areaAssertions.stream().map(NormalizedAssertion::getSourceId).distinct().toList())
                                .conflictingAssertionIds(areaAssertions.stream().map(NormalizedAssertion::getEvidenceId).toList())
                                .severity("MEDIUM")
                                .explanation(String.format("Area discrepancy exceeds 1.5%% tolerance (observed min: %s sqm, max: %s sqm, variance: %s%%).",
                                        min, max, pct.setScale(2, RoundingMode.HALF_UP)))
                                .build());
                    }
                }
            }
        }

        return witnesses;
    }
}
