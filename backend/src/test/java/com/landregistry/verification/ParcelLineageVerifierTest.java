package com.landregistry.verification;

import com.landregistry.model.MutationRequest;
import com.landregistry.model.Parcel;
import com.landregistry.verification.m2.LineageVerificationResult;
import com.landregistry.verification.m2.ParcelLineageVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParcelLineageVerifierTest {

    private ParcelLineageVerifier verifier;

    @BeforeEach
    public void setup() {
        verifier = new ParcelLineageVerifier();
    }

    @Test
    public void testValidTransferLineage() {
        Parcel p = Parcel.builder()
                .id("p-1")
                .parcelCode("PARCEL-1")
                .status("ACTIVE")
                .version(1)
                .build();

        MutationRequest mut = MutationRequest.builder()
                .id("m-1")
                .mutationType("TRANSFER")
                .expectedVersions("{\"p-1\": 1}")
                .build();

        LineageVerificationResult result = verifier.verifyMutationLineage(mut, List.of(p));
        assertTrue(result.isValid(), "Transfer with matching version must be valid");
    }

    @Test
    public void testStaleVersionTransferRejected() {
        Parcel p = Parcel.builder()
                .id("p-1")
                .parcelCode("PARCEL-1")
                .status("ACTIVE")
                .version(2) // current version is 2
                .build();

        MutationRequest mut = MutationRequest.builder()
                .id("m-1")
                .mutationType("TRANSFER")
                .expectedVersions("{\"p-1\": 1}") // expects stale v1
                .build();

        LineageVerificationResult result = verifier.verifyMutationLineage(mut, List.of(p));
        assertFalse(result.isValid(), "Transfer expecting stale version must be rejected");
        assertTrue(result.getExplanation().contains("Stale parcel version"));
    }

    @Test
    public void testValidSplitConservingArea() {
        // Parent: 100 x 100 = 10,000 sqm
        Parcel parent = Parcel.builder()
                .id("parent-1")
                .parcelCode("P-PARENT")
                .status("ACTIVE")
                .version(1)
                .areaSqm(BigDecimal.valueOf(10000.0))
                .geometryWkt("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))")
                .build();

        // Split into:
        // Child A: 0..60 x 0..100 = 6,000 sqm
        // Child B: 60..100 x 0..100 = 4,000 sqm
        String payload = """
        {
            "children": [
                {"parcelCode": "P-A", "areaSqm": 6000.0, "geometryWkt": "POLYGON ((0 0, 60 0, 60 100, 0 100, 0 0))"},
                {"parcelCode": "P-B", "areaSqm": 4000.0, "geometryWkt": "POLYGON ((60 0, 100 0, 100 100, 60 100, 60 0))"}
            ]
        }
        """;

        MutationRequest mut = MutationRequest.builder()
                .id("m-split")
                .mutationType("SPLIT")
                .expectedVersions("{\"parent-1\": 1}")
                .proposedPayload(payload)
                .build();

        LineageVerificationResult result = verifier.verifyMutationLineage(mut, List.of(parent));
        assertTrue(result.isValid(), "Split conserving area with disjoint children must pass");
    }

    @Test
    public void testSplitWithOverlappingChildrenRejected() {
        Parcel parent = Parcel.builder()
                .id("parent-1")
                .parcelCode("P-PARENT")
                .status("ACTIVE")
                .version(1)
                .areaSqm(BigDecimal.valueOf(10000.0))
                .geometryWkt("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))")
                .build();

        // Children overlap between x = 50 and x = 60
        String payload = """
        {
            "children": [
                {"parcelCode": "P-A", "areaSqm": 6000.0, "geometryWkt": "POLYGON ((0 0, 60 0, 60 100, 0 100, 0 0))"},
                {"parcelCode": "P-B", "areaSqm": 5000.0, "geometryWkt": "POLYGON ((50 0, 100 0, 100 100, 50 100, 50 0))"}
            ]
        }
        """;

        MutationRequest mut = MutationRequest.builder()
                .id("m-split-overlap")
                .mutationType("SPLIT")
                .expectedVersions("{\"parent-1\": 1}")
                .proposedPayload(payload)
                .build();

        LineageVerificationResult result = verifier.verifyMutationLineage(mut, List.of(parent));
        assertFalse(result.isValid(), "Overlapping children must be rejected");
        assertTrue(result.getExplanation().contains("overlap"));
    }

    @Test
    public void testSplitWithAreaNonConservationRejected() {
        Parcel parent = Parcel.builder()
                .id("parent-1")
                .parcelCode("P-PARENT")
                .status("ACTIVE")
                .version(1)
                .areaSqm(BigDecimal.valueOf(10000.0))
                .geometryWkt("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))")
                .build();

        // Children sum to only 8,000 sqm instead of 10,000 sqm
        String payload = """
        {
            "children": [
                {"parcelCode": "P-A", "areaSqm": 5000.0, "geometryWkt": "POLYGON ((0 0, 50 0, 50 100, 0 100, 0 0))"},
                {"parcelCode": "P-B", "areaSqm": 3000.0, "geometryWkt": "POLYGON ((70 0, 100 0, 100 100, 70 100, 70 0))"}
            ]
        }
        """;

        MutationRequest mut = MutationRequest.builder()
                .id("m-split-gap")
                .mutationType("SPLIT")
                .expectedVersions("{\"parent-1\": 1}")
                .proposedPayload(payload)
                .build();

        LineageVerificationResult result = verifier.verifyMutationLineage(mut, List.of(parent));
        assertFalse(result.isValid(), "Split violating area conservation must be rejected");
        assertTrue(result.getExplanation().contains("Area conservation violated"));
    }
}
