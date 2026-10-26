package com.landregistry.verification;

import com.landregistry.model.DecisionCertificate;
import com.landregistry.model.MutationRequest;
import com.landregistry.model.User;
import com.landregistry.verification.certificate.DecisionCertificateEngine;
import com.landregistry.verification.m2.LineageVerificationResult;
import com.landregistry.verification.m5.ConflictWitness;
import com.landregistry.verification.m8.DecisionType;
import com.landregistry.verification.m8.PredicateEvaluation;
import com.landregistry.verification.m8.PredicateStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionCertificateTest {

    private DecisionCertificateEngine engine;

    @BeforeEach
    public void setup() {
        engine = new DecisionCertificateEngine();
    }

    @Test
    public void testCertificateGenerationAndHashing() {
        User user = User.builder().id("u-1").login("alice").build();
        MutationRequest mutation = MutationRequest.builder()
                .id("mut-101")
                .mutationType("TRANSFER")
                .targetParcelIds("[\"p-1\"]")
                .expectedVersions("{\"p-1\": 1}")
                .requestedBy(user)
                .build();

        List<PredicateEvaluation> preds = List.of(
                PredicateEvaluation.builder().predicateCode("P1").status(PredicateStatus.SATISFIED).explanation("OK").build()
        );

        LineageVerificationResult lineage = LineageVerificationResult.builder().valid(true).explanation("Passed").build();

        DecisionCertificate cert1 = engine.generateCertificate(mutation, DecisionType.AUTHORIZE, preds, Collections.emptyList(), lineage);
        assertNotNull(cert1.getCertificateHash());
        assertEquals(64, cert1.getCertificateHash().length(), "SHA-256 hash must be 64 hex characters");
        assertEquals("AUTHORIZE", cert1.getDecision());

        // Verify that canonical JSON contains ordered keys
        assertTrue(cert1.getCanonicalPayload().contains("\"certificateVersion\":\"1.0\""));
        assertTrue(cert1.getCanonicalPayload().contains("\"mutationId\":\"mut-101\""));
    }
}
