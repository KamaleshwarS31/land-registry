package com.landregistry.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landregistry.blockchain.BlockchainCommitResult;
import com.landregistry.blockchain.Web3jBlockchainService;
import com.landregistry.model.*;
import com.landregistry.repository.*;
import com.landregistry.service.DemoResetService;
import com.landregistry.service.MutationWorkflowService;
import com.landregistry.verification.certificate.DecisionCertificateEngine;
import com.landregistry.verification.m2.LineageVerificationResult;
import com.landregistry.verification.m2.ParcelLineageVerifier;
import com.landregistry.verification.m5.ConflictWitness;
import com.landregistry.verification.m5.ContradictionReconciliationEngine;
import com.landregistry.verification.m5.NormalizedAssertion;
import com.landregistry.verification.m8.DecisionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class FinalAcceptanceTestSuite {

    @Autowired
    private DemoResetService demoResetService;

    @Autowired
    private MutationWorkflowService mutationWorkflowService;

    @Autowired
    private ParcelRepository parcelRepository;

    @Autowired
    private EvidenceRecordRepository evidenceRecordRepository;

    @Autowired
    private MutationRequestRepository mutationRequestRepository;

    @Autowired
    private DecisionCertificateRepository certificateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Web3jBlockchainService blockchainService;

    @Autowired
    private ParcelLineageVerifier lineageVerifier;

    @Autowired
    private ContradictionReconciliationEngine contradictionEngine;

    @Autowired
    private DecisionCertificateEngine certificateEngine;

    private User alice;
    private User bob;
    private User clara;
    private User eve;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Run acceptance tests in SIMULATION mode unless specifically testing LIVE node behavior
        blockchainService.setMode("SIMULATION");

        // Reset to canonical baseline scenarios (P-101 to P-405) before each test
        demoResetService.resetDemoData();
        alice = userRepository.findById("u-alice").orElseThrow();
        bob = userRepository.findById("u-bob").orElseThrow();
        clara = userRepository.findById("u-clara").orElseThrow();
        eve = userRepository.findById("u-eve").orElseThrow();
    }

    @Test
    @DisplayName("T01: Register a valid new parcel with valid evidence")
    void testT01_RegisterValidParcel() {
        Map<String, Object> payload = Map.of(
                "parcelCode", "P-NEW-999",
                "surveyNumber", "SY-2026-999",
                "locality", "Greenfield Sector 9",
                "areaSqm", 5000.0,
                "geometryWkt", "POLYGON ((0 0, 50 0, 50 100, 0 100, 0 0))"
        );

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "REGISTRATION", Collections.emptyList(), Collections.emptyMap(), payload, alice
        );

        // Upload registration deed evidence
        evidenceRecordRepository.save(EvidenceRecord.builder()
                .id("ev-reg-999")
                .requestId(mutation.getId())
                .sourceId("REGISTRATION_DEED")
                .sourceRecordId("DEED-999")
                .attributeName("OWNER_NAME")
                .attributeValue("Alice Vance")
                .versionLabel("v1.0")
                .observedAt(java.time.Instant.now())
                .fileName("alice_reg_deed.pdf")
                .filePath("land-evidence/P-NEW-999/deed.pdf")
                .storageProvider("LOCAL")
                .sha256Hash("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")
                .uploadedBy(alice)
                .createdAt(java.time.Instant.now())
                .build());

        evidenceRecordRepository.save(EvidenceRecord.builder()
                .id("ev-reg-survey-999")
                .requestId(mutation.getId())
                .sourceId("CADASTRAL_SURVEY")
                .sourceRecordId("SURVEY-999")
                .attributeName("SURVEY_AREA_SQM")
                .attributeValue("5000.0")
                .versionLabel("v1.0")
                .observedAt(java.time.Instant.now())
                .fileName("survey_999.pdf")
                .filePath("land-evidence/P-NEW-999/survey.pdf")
                .storageProvider("LOCAL")
                .sha256Hash("d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35")
                .uploadedBy(alice)
                .createdAt(java.time.Instant.now())
                .build());

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("AUTHORIZE", eval.get("decision"));

        Map<String, Object> commit = mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara);
        assertEquals("COMMITTED", commit.get("status"));
        assertNotNull(commit.get("certificateHash"));

        Parcel created = parcelRepository.findByParcelCode("P-NEW-999").orElse(null);
        assertNotNull(created);
        assertEquals("ACTIVE", created.getStatus());
        assertEquals(1, created.getVersion());
    }

    @Test
    @DisplayName("T02: Transfer P-101 happy path (all evidence valid, no contradictions)")
    void testT02_TransferHappyPath() {
        Parcel p101 = parcelRepository.findByParcelCode("P-101").orElseThrow();
        assertEquals(1, p101.getVersion());
        assertEquals("u-alice", p101.getOwner().getId());

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p101.getId()),
                Map.of(p101.getId(), 1),
                Map.of("buyerUserId", bob.getId(), "saleConsideration", "500000"),
                alice
        );

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("AUTHORIZE", eval.get("decision"), "P-101 has all valid evidence and must AUTHORIZE");

        Map<String, Object> commit = mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara);
        assertEquals("COMMITTED", commit.get("status"));

        Parcel updated = parcelRepository.findByParcelCode("P-101").orElseThrow();
        assertEquals(2, updated.getVersion(), "Committed transfer must increment parcel version to 2");
        assertEquals("u-bob", updated.getOwner().getId(), "Committed transfer must update owner to Bob");
    }

    @Test
    @DisplayName("T03: Transfer P-202 when mortgage/encumbrance evidence missing (HOLD)")
    void testT03_TransferMissingMortgageHold() {
        Parcel p202 = parcelRepository.findByParcelCode("P-202").orElseThrow();

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p202.getId()),
                Map.of(p202.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("HOLD", eval.get("decision"), "Missing mortgage clearance record must evaluate to HOLD");
        assertEquals("HELD", eval.get("status"));

        // Strict Server-Side Gate: commit MUST throw IllegalStateException and block contract settlement
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara)
        );
        assertTrue(ex.getMessage().contains("HOLD") || ex.getMessage().contains("READY_FOR_COMMIT"));

        // Verify P-202 state was not mutated
        Parcel unchanged = parcelRepository.findByParcelCode("P-202").orElseThrow();
        assertEquals(1, unchanged.getVersion());
        assertEquals("u-alice", unchanged.getOwner().getId());
    }

    @Test
    @DisplayName("T04: Conflicting owner assertions for P-303 (REJECT)")
    void testT04_ConflictingOwnerAssertionsReject() {
        Parcel p303 = parcelRepository.findByParcelCode("P-303").orElseThrow();

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p303.getId()),
                Map.of(p303.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("REJECT", eval.get("decision"), "Cross-registry ownership collision must evaluate to REJECT");
        assertEquals("REJECTED", eval.get("status"));

        // Server-side settlement gate blocks commit
        assertThrows(IllegalStateException.class, () ->
                mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara)
        );
    }

    @Test
    @DisplayName("T05: Survey/deed area deviation > 1.5% emits medium witness")
    void testT05_SurveyDeedAreaDeviationWitness() {
        List<NormalizedAssertion> assertions = List.of(
                NormalizedAssertion.builder()
                        .sourceId("REGISTRATION_DEED")
                        .evidenceId("ev-d1")
                        .attributeName("SURVEY_AREA_SQM")
                        .rawValue("10000.0")
                        .normalizedValue("10000.00")
                        .build(),
                NormalizedAssertion.builder()
                        .sourceId("CADASTRAL_SURVEY")
                        .evidenceId("ev-s1")
                        .attributeName("SURVEY_AREA_SQM")
                        .rawValue("10250.0") // 2.5% deviation > 1.5% threshold
                        .normalizedValue("10250.00")
                        .build()
        );

        List<ConflictWitness> witnesses = contradictionEngine.detectContradictions(assertions);
        assertFalse(witnesses.isEmpty());
        ConflictWitness areaWitness = witnesses.stream()
                .filter(w -> "RULE_SURVEY_BOUNDARY_DEVIATION".equals(w.getRuleCode()))
                .findFirst()
                .orElse(null);

        assertNotNull(areaWitness);
        assertEquals("MEDIUM", areaWitness.getSeverity());
        assertTrue(areaWitness.getExplanation().contains("1.5%"));
    }

    @Test
    @DisplayName("T06: Valid split of P-404 into two non-overlapping children (6000 + 4000 sqm)")
    void testT06_ValidSplitLineageAndInvariants() {
        Parcel p404 = parcelRepository.findByParcelCode("P-404").orElseThrow();

        Map<String, Object> child1 = Map.of(
                "parcelCode", "P-404-A",
                "areaSqm", 6000.0,
                "geometryWkt", "POLYGON ((0 150, 60 150, 60 250, 0 250, 0 150))",
                "ownerUserId", alice.getId()
        );
        Map<String, Object> child2 = Map.of(
                "parcelCode", "P-404-B",
                "areaSqm", 4000.0,
                "geometryWkt", "POLYGON ((60 150, 100 150, 100 250, 60 250, 60 150))",
                "ownerUserId", bob.getId()
        );

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "SPLIT",
                List.of(p404.getId()),
                Map.of(p404.getId(), 1),
                Map.of("children", List.of(child1, child2)),
                alice
        );

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("AUTHORIZE", eval.get("decision"));

        Map<String, Object> commit = mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara);
        assertEquals("COMMITTED", commit.get("status"));

        // Parent must be retired (INACTIVE_SPLIT)
        Parcel retiredParent = parcelRepository.findById(p404.getId()).orElseThrow();
        assertEquals("INACTIVE_SPLIT", retiredParent.getStatus());

        // Children must be active
        Parcel cA = parcelRepository.findByParcelCode("P-404-A").orElseThrow();
        Parcel cB = parcelRepository.findByParcelCode("P-404-B").orElseThrow();
        assertEquals("ACTIVE", cA.getStatus());
        assertEquals("ACTIVE", cB.getStatus());
    }

    @Test
    @DisplayName("T07: Split with overlapping children is rejected by M2")
    void testT07_SplitOverlappingChildrenRejected() {
        Parcel p405 = parcelRepository.findByParcelCode("P-405").orElseThrow();

        // Deliberate overlap: child 1 extends from 100 to 150, child 2 extends from 140 to 180 (10m overlap)
        Map<String, Object> child1 = Map.of(
                "parcelCode", "P-405-A",
                "areaSqm", 5000.0,
                "geometryWkt", "POLYGON ((100 150, 150 150, 150 250, 100 250, 100 150))"
        );
        Map<String, Object> child2 = Map.of(
                "parcelCode", "P-405-B",
                "areaSqm", 4000.0,
                "geometryWkt", "POLYGON ((140 150, 180 150, 180 250, 140 250, 140 150))"
        );

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "SPLIT",
                List.of(p405.getId()),
                Map.of(p405.getId(), 1),
                Map.of("children", List.of(child1, child2)),
                alice
        );

        LineageVerificationResult res = lineageVerifier.verifyMutationLineage(mutation, List.of(p405));
        assertFalse(res.isValid(), "Overlapping children must fail M2 verification");
        assertTrue(res.getExplanation().toLowerCase().contains("overlap"));

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("REJECT", eval.get("decision"));
    }

    @Test
    @DisplayName("T08: Split with boundary gap / area mismatch is rejected by M2")
    void testT08_SplitAreaMismatchRejected() {
        Parcel p405 = parcelRepository.findByParcelCode("P-405").orElseThrow(); // 8000 sqm

        // Children only sum to 6000 sqm (gap of 2000 sqm)
        Map<String, Object> child1 = Map.of(
                "parcelCode", "P-405-A",
                "areaSqm", 3000.0,
                "geometryWkt", "POLYGON ((100 150, 130 150, 130 250, 100 250, 100 150))"
        );
        Map<String, Object> child2 = Map.of(
                "parcelCode", "P-405-B",
                "areaSqm", 3000.0,
                "geometryWkt", "POLYGON ((150 150, 180 150, 180 250, 150 250, 150 150))"
        );

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "SPLIT",
                List.of(p405.getId()),
                Map.of(p405.getId(), 1),
                Map.of("children", List.of(child1, child2)),
                alice
        );

        LineageVerificationResult res = lineageVerifier.verifyMutationLineage(mutation, List.of(p405));
        assertFalse(res.isValid(), "Boundary gap / area deficit must fail M2 verification");
        assertTrue(res.getExplanation().toLowerCase().contains("gap") || res.getExplanation().toLowerCase().contains("conservation"));
    }

    @Test
    @DisplayName("T09: Stale expected version after concurrent transfer causes commit failure")
    void testT09_StaleExpectedVersionReverts() {
        Parcel p101 = parcelRepository.findByParcelCode("P-101").orElseThrow();

        // Transfer 1: Alice -> Bob, succeeds, increments version to 2
        MutationRequest mutation1 = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p101.getId()),
                Map.of(p101.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );
        mutationWorkflowService.evaluateMutation(mutation1.getId());
        mutationWorkflowService.commitMutationToBlockchain(mutation1.getId(), clara);

        // Transfer 2: Stale mutation that evaluated against version 1
        MutationRequest staleMutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p101.getId()),
                Map.of(p101.getId(), 1), // STALE expected version (actual is now 2)
                Map.of("buyerUserId", eve.getId()),
                bob
        );

        // Atomic re-check at commit time MUST detect version mismatch and fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                mutationWorkflowService.commitMutationToBlockchain(staleMutation.getId(), clara)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("version") || ex.getMessage().toLowerCase().contains("ready_for_commit"));
    }

    @Test
    @DisplayName("T10: Registrar-only review enforcement blocks unauthorized actions on HOLD/REJECT")
    void testT10_RbacUnauthorizedAccessDenied() {
        Parcel p202 = parcelRepository.findByParcelCode("P-202").orElseThrow();

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p202.getId()),
                Map.of(p202.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );
        mutationWorkflowService.evaluateMutation(mutation.getId()); // Results in HELD

        // Registrar attempts to APPROVE a HELD request without resolving evidence -> must fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                mutationWorkflowService.reviewMutation(mutation.getId(), "APPROVE", "Attempted bypass", clara)
        );
        assertTrue(ex.getMessage().contains("rejected"));
    }

    @Test
    @DisplayName("T11: Evidence hashing integrity produces reproducible digest")
    void testT11_EvidenceHashingIntegrity() throws Exception {
        byte[] content1 = "Standard Cadastral Boundary Survey 2026".getBytes(StandardCharsets.UTF_8);
        byte[] content2 = "Altered Cadastral Boundary Survey 2026".getBytes(StandardCharsets.UTF_8);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String hash1 = HexFormat.of().formatHex(md.digest(content1));
        md.reset();
        String hash2 = HexFormat.of().formatHex(md.digest(content2));

        assertNotEquals(hash1, hash2);

        md.reset();
        String hash1Repeat = HexFormat.of().formatHex(md.digest(content1));
        assertEquals(hash1, hash1Repeat, "Same file content must always yield identical SHA-256 digest");
    }

    @Test
    @DisplayName("T12: Node unavailable in LIVE mode fails closed with clear error")
    void testT12_NodeUnavailableFailsClosed() {
        // Explicitly set service to LIVE mode with an unreachable RPC URL
        Web3jBlockchainService strictService = new Web3jBlockchainService(
                "http://127.0.0.1:9999", // Unreachable port
                "0x5FbDB2315678afecb367f032d93F642f64180aa3",
                "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80",
                31337L,
                "LIVE"
        );

        BlockchainCommitResult res = strictService.transferOwnershipOnChain(
                "P-101", "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC", 1, "0x1234"
        );

        assertFalse(res.isSuccess(), "LIVE mode must fail closed when EVM node is offline");
        assertNull(res.getTxHash(), "LIVE mode must NOT generate synthetic transaction hashes when offline");
        assertFalse(res.isSimulated());
        assertTrue(res.getErrorMessage().contains("unreachable") || res.getErrorMessage().contains("failed"));
    }

    @Test
    @DisplayName("T13: Tampered certificate payload fails digest verification")
    void testT13_TamperedCertificateDigestFails() {
        Parcel p101 = parcelRepository.findByParcelCode("P-101").orElseThrow();
        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p101.getId()),
                Map.of(p101.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );

        mutationWorkflowService.evaluateMutation(mutation.getId());
        DecisionCertificate cert = certificateRepository.findByMutationId(mutation.getId()).orElseThrow();

        // 1. Verify original payload matches
        assertTrue(certificateEngine.verifyDigest(cert.getCanonicalPayload(), cert.getCertificateHash()));

        // 2. Tamper payload by modifying 1 character
        String tampered = cert.getCanonicalPayload().replace("AUTHORIZE", "HOLD");
        assertFalse(certificateEngine.verifyDigest(tampered, cert.getCertificateHash()),
                "Tampered certificate payload must fail cryptographic verification (T13)");
    }

    @Test
    @DisplayName("T14: Repeating settle request detects mutation already committed")
    void testT14_RequestSubmitIdempotency() {
        Parcel p101 = parcelRepository.findByParcelCode("P-101").orElseThrow();
        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "TRANSFER",
                List.of(p101.getId()),
                Map.of(p101.getId(), 1),
                Map.of("buyerUserId", bob.getId()),
                alice
        );

        mutationWorkflowService.evaluateMutation(mutation.getId());
        mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara);

        // Attempting to commit again on the same mutation request must fail
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara)
        );
        assertTrue(ex.getMessage().contains("READY_FOR_COMMIT"));
    }

    @Test
    @DisplayName("T15: Merge two valid active parcels into a single child")
    void testT15_MergeTwoValidParcels() {
        Parcel p101 = parcelRepository.findByParcelCode("P-101").orElseThrow(); // 10,000 sqm
        Parcel p202 = parcelRepository.findByParcelCode("P-202").orElseThrow(); // 7,500 sqm

        // Add missing tax clearance for P-202 so it can be merged cleanly
        evidenceRecordRepository.save(EvidenceRecord.builder()
                .id("ev-202-tax-merge")
                .parcel(p202)
                .sourceId("REVENUE_RECORD")
                .sourceRecordId("TAX-202-M")
                .attributeName("TAX_CLEARANCE")
                .attributeValue("PAID_CLEARED")
                .versionLabel("v1.0")
                .observedAt(java.time.Instant.now())
                .fileName("tax_202.pdf")
                .filePath("land-evidence/P-202/tax.pdf")
                .storageProvider("LOCAL")
                .sha256Hash("4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a")
                .uploadedBy(alice)
                .createdAt(java.time.Instant.now())
                .build());

        Map<String, Object> mergePayload = Map.of(
                "mergedParcelCode", "P-MERGED-101-202",
                "mergedAreaSqm", 17500.0,
                "mergedGeometryWkt", "POLYGON ((0 0, 175 0, 175 100, 0 100, 0 0))",
                "ownerUserId", alice.getId()
        );

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                "MERGE",
                List.of(p101.getId(), p202.getId()),
                Map.of(p101.getId(), 1, p202.getId(), 1),
                mergePayload,
                alice
        );

        Map<String, Object> eval = mutationWorkflowService.evaluateMutation(mutation.getId());
        assertEquals("AUTHORIZE", eval.get("decision"));

        Map<String, Object> commit = mutationWorkflowService.commitMutationToBlockchain(mutation.getId(), clara);
        assertEquals("COMMITTED", commit.get("status"));

        // Both parents must now be retired (INACTIVE_MERGED)
        assertEquals("INACTIVE_MERGED", parcelRepository.findById(p101.getId()).orElseThrow().getStatus());
        assertEquals("INACTIVE_MERGED", parcelRepository.findById(p202.getId()).orElseThrow().getStatus());

        // Merged parcel must be ACTIVE
        Parcel merged = parcelRepository.findByParcelCode("P-MERGED-101-202").orElseThrow();
        assertEquals("ACTIVE", merged.getStatus());
        assertEquals(new BigDecimal("17500.0000"), merged.getAreaSqm());
    }
}
