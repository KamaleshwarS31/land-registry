package com.landregistry.verification;

import com.landregistry.model.MutationRequest;
import com.landregistry.model.Parcel;
import com.landregistry.model.User;
import com.landregistry.verification.m5.ConflictWitness;
import com.landregistry.verification.m8.DecisionType;
import com.landregistry.verification.m8.PredicateEvaluation;
import com.landregistry.verification.m8.PredicateStatus;
import com.landregistry.verification.m8.UncertaintyEvaluationEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UncertaintyEvaluationTest {

    private UncertaintyEvaluationEngine engine;

    @BeforeEach
    public void setup() {
        engine = new UncertaintyEvaluationEngine();
    }

    @Test
    public void testAllSatisfiedYieldsAuthorize() {
        List<PredicateEvaluation> predicates = List.of(
                PredicateEvaluation.builder().predicateCode("P1").mandatory(true).status(PredicateStatus.SATISFIED).build(),
                PredicateEvaluation.builder().predicateCode("P2").mandatory(true).status(PredicateStatus.SATISFIED).build(),
                PredicateEvaluation.builder().predicateCode("P3").mandatory(false).status(PredicateStatus.UNRESOLVED).build()
        );

        DecisionType decision = engine.evaluateDecision(predicates);
        assertEquals(DecisionType.AUTHORIZE, decision);
    }

    @Test
    public void testViolatedMandatoryConditionYieldsReject() {
        List<PredicateEvaluation> predicates = List.of(
                PredicateEvaluation.builder().predicateCode("P1").mandatory(true).status(PredicateStatus.SATISFIED).build(),
                PredicateEvaluation.builder().predicateCode("P2").mandatory(true).status(PredicateStatus.VIOLATED).build(),
                PredicateEvaluation.builder().predicateCode("P3").mandatory(true).status(PredicateStatus.UNRESOLVED).build()
        );

        DecisionType decision = engine.evaluateDecision(predicates);
        assertEquals(DecisionType.REJECT, decision, "Violated condition must always trigger REJECT, overriding UNRESOLVED");
    }

    @Test
    public void testUnresolvedMandatoryConditionYieldsHold() {
        List<PredicateEvaluation> predicates = List.of(
                PredicateEvaluation.builder().predicateCode("P1").mandatory(true).status(PredicateStatus.SATISFIED).build(),
                PredicateEvaluation.builder().predicateCode("P2").mandatory(true).status(PredicateStatus.UNRESOLVED).build()
        );

        DecisionType decision = engine.evaluateDecision(predicates);
        assertEquals(DecisionType.HOLD, decision, "Unresolved mandatory predicate must trigger HOLD to prevent unsafe approval");
    }

    @Test
    public void testMissingEvidenceNeverDefaultsToSatisfied() {
        User user = User.builder().id("u-1").login("alice").build();
        MutationRequest mutation = MutationRequest.builder()
                .id("mut-1")
                .mutationType("TRANSFER")
                .requestedBy(user)
                .build();

        Parcel parcel = Parcel.builder()
                .id("p-1")
                .owner(user)
                .status("ACTIVE")
                .version(1)
                .areaSqm(BigDecimal.valueOf(10000))
                .build();

        // No evidence records provided at all
        List<PredicateEvaluation> evaluated = engine.evaluatePredicates(
                mutation, List.of(parcel), Collections.emptyList(), Collections.emptyList(), true, "Lineage OK"
        );

        // Assert that encumbrance and survey predicates are UNRESOLVED, not SATISFIED!
        PredicateEvaluation encPred = evaluated.stream()
                .filter(p -> "PRED_ENCUMBRANCE_CLEAR".equals(p.getPredicateCode()))
                .findFirst().orElseThrow();
        assertEquals(PredicateStatus.UNRESOLVED, encPred.getStatus());

        DecisionType decision = engine.evaluateDecision(evaluated);
        assertEquals(DecisionType.HOLD, decision, "Missing evidence must result in HOLD");
    }
}
