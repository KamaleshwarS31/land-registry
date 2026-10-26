package com.landregistry.verification;

import com.landregistry.model.EvidenceRecord;
import com.landregistry.verification.m5.ConflictWitness;
import com.landregistry.verification.m5.ContradictionReconciliationEngine;
import com.landregistry.verification.m5.NormalizedAssertion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContradictionReconciliationTest {

    private ContradictionReconciliationEngine engine;

    @BeforeEach
    public void setup() {
        engine = new ContradictionReconciliationEngine();
    }

    @Test
    public void testCleanAssertionsProduceNoWitnesses() {
        EvidenceRecord rec1 = EvidenceRecord.builder()
                .id("ev-1")
                .sourceId("REGISTRATION_DEED")
                .sourceRecordId("DEED-101")
                .attributeName("OWNER_NAME")
                .attributeValue("Alice Vance")
                .observedAt(Instant.now())
                .build();

        EvidenceRecord rec2 = EvidenceRecord.builder()
                .id("ev-2")
                .sourceId("CADASTRAL_SURVEY")
                .sourceRecordId("SURVEY-101")
                .attributeName("SURVEY_AREA_SQM")
                .attributeValue("10000.0")
                .observedAt(Instant.now())
                .build();

        List<NormalizedAssertion> norm = engine.normalizeAssertions(List.of(rec1, rec2));
        List<ConflictWitness> witnesses = engine.detectContradictions(norm);

        assertTrue(witnesses.isEmpty(), "Clean records must produce no conflict witnesses");
    }

    @Test
    public void testOwnershipCollisionDetectedAcrossSources() {
        EvidenceRecord deedClaim = EvidenceRecord.builder()
                .id("ev-deed")
                .sourceId("REGISTRATION_DEED")
                .sourceRecordId("DEED-2022")
                .attributeName("OWNER_NAME")
                .attributeValue("Alice Vance")
                .observedAt(Instant.now())
                .build();

        EvidenceRecord courtClaim = EvidenceRecord.builder()
                .id("ev-court")
                .sourceId("COURT_ORDER")
                .sourceRecordId("CASE-2023-99")
                .attributeName("OWNER_NAME")
                .attributeValue("Mallory Trant")
                .observedAt(Instant.now())
                .build();

        List<NormalizedAssertion> norm = engine.normalizeAssertions(List.of(deedClaim, courtClaim));
        List<ConflictWitness> witnesses = engine.detectContradictions(norm);

        assertEquals(1, witnesses.size(), "Should detect exactly 1 conflict witness");
        ConflictWitness w = witnesses.get(0);
        assertEquals("RULE_OWNERSHIP_COLLISION", w.getRuleCode());
        assertEquals("HIGH", w.getSeverity());
        assertTrue(w.getExplanation().contains("Alice Vance") || w.getExplanation().contains("alice vance"));
    }

    @Test
    public void testActiveEncumbranceDetected() {
        EvidenceRecord mortgageRec = EvidenceRecord.builder()
                .id("ev-bank")
                .sourceId("MORTGAGE_REGISTRY")
                .sourceRecordId("MORT-2021-44")
                .attributeName("ENCUMBRANCE_STATUS")
                .attributeValue("ACTIVE_MORTGAGE_DEFAULT")
                .observedAt(Instant.now())
                .build();

        List<NormalizedAssertion> norm = engine.normalizeAssertions(List.of(mortgageRec));
        List<ConflictWitness> witnesses = engine.detectContradictions(norm);

        assertEquals(1, witnesses.size());
        assertEquals("RULE_ACTIVE_ENCUMBRANCE", witnesses.get(0).getRuleCode());
        assertEquals("HIGH", witnesses.get(0).getSeverity());
    }

    @Test
    public void testSurveyDeviationExceedingToleranceDetected() {
        EvidenceRecord survey1 = EvidenceRecord.builder()
                .id("ev-s1")
                .sourceId("CADASTRAL_SURVEY")
                .attributeName("SURVEY_AREA_SQM")
                .attributeValue("10000.0")
                .observedAt(Instant.now())
                .build();

        EvidenceRecord survey2 = EvidenceRecord.builder()
                .id("ev-s2")
                .sourceId("REGISTRATION_DEED")
                .attributeName("SURVEY_AREA_SQM")
                .attributeValue("10300.0") // 3% deviation > 1.5% tolerance
                .observedAt(Instant.now())
                .build();

        List<NormalizedAssertion> norm = engine.normalizeAssertions(List.of(survey1, survey2));
        List<ConflictWitness> witnesses = engine.detectContradictions(norm);

        assertEquals(1, witnesses.size());
        assertEquals("RULE_SURVEY_BOUNDARY_DEVIATION", witnesses.get(0).getRuleCode());
        assertEquals("MEDIUM", witnesses.get(0).getSeverity());
    }
}
