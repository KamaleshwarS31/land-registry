package com.landregistry.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.landregistry.blockchain.BlockchainCommitResult;
import com.landregistry.blockchain.Web3jBlockchainService;
import com.landregistry.model.*;
import com.landregistry.repository.*;
import com.landregistry.verification.certificate.DecisionCertificateEngine;
import com.landregistry.verification.m2.LineageVerificationResult;
import com.landregistry.verification.m2.ParcelLineageVerifier;
import com.landregistry.verification.m5.ConflictWitness;
import com.landregistry.verification.m5.ContradictionReconciliationEngine;
import com.landregistry.verification.m5.NormalizedAssertion;
import com.landregistry.verification.m8.DecisionType;
import com.landregistry.verification.m8.PredicateEvaluation;
import com.landregistry.verification.m8.UncertaintyEvaluationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class MutationWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(MutationWorkflowService.class);

    private final MutationRequestRepository mutationRequestRepository;
    private final ParcelRepository parcelRepository;
    private final EvidenceRecordRepository evidenceRecordRepository;
    private final PredicateResultRepository predicateResultRepository;
    private final DecisionCertificateRepository decisionCertificateRepository;
    private final BlockchainTransactionRepository blockchainTransactionRepository;
    private final ParcelLineageRepository parcelLineageRepository;
    private final UserRepository userRepository;

    private final ContradictionReconciliationEngine contradictionEngine;
    private final UncertaintyEvaluationEngine uncertaintyEngine;
    private final ParcelLineageVerifier lineageVerifier;
    private final DecisionCertificateEngine certificateEngine;
    private final Web3jBlockchainService blockchainService;
    private final AuditEventService auditEventService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MutationWorkflowService(
            MutationRequestRepository mutationRequestRepository,
            ParcelRepository parcelRepository,
            EvidenceRecordRepository evidenceRecordRepository,
            PredicateResultRepository predicateResultRepository,
            DecisionCertificateRepository decisionCertificateRepository,
            BlockchainTransactionRepository blockchainTransactionRepository,
            ParcelLineageRepository parcelLineageRepository,
            UserRepository userRepository,
            ContradictionReconciliationEngine contradictionEngine,
            UncertaintyEvaluationEngine uncertaintyEngine,
            ParcelLineageVerifier lineageVerifier,
            DecisionCertificateEngine certificateEngine,
            Web3jBlockchainService blockchainService,
            AuditEventService auditEventService
    ) {
        this.mutationRequestRepository = mutationRequestRepository;
        this.parcelRepository = parcelRepository;
        this.evidenceRecordRepository = evidenceRecordRepository;
        this.predicateResultRepository = predicateResultRepository;
        this.decisionCertificateRepository = decisionCertificateRepository;
        this.blockchainTransactionRepository = blockchainTransactionRepository;
        this.parcelLineageRepository = parcelLineageRepository;
        this.userRepository = userRepository;
        this.contradictionEngine = contradictionEngine;
        this.uncertaintyEngine = uncertaintyEngine;
        this.lineageVerifier = lineageVerifier;
        this.certificateEngine = certificateEngine;
        this.blockchainService = blockchainService;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public MutationRequest submitMutation(
            String mutationType,
            List<String> targetParcelIds,
            Map<String, Integer> expectedVersions,
            Map<String, Object> proposedPayload,
            User requester
    ) {
        try {
            String targetJson = objectMapper.writeValueAsString(targetParcelIds);
            String versionsJson = objectMapper.writeValueAsString(expectedVersions);
            String payloadJson = objectMapper.writeValueAsString(proposedPayload);

            MutationRequest request = MutationRequest.builder()
                    .id(UUID.randomUUID().toString())
                    .mutationType(mutationType.toUpperCase())
                    .targetParcelIds(targetJson)
                    .expectedVersions(versionsJson)
                    .proposedPayload(payloadJson)
                    .requestedBy(requester)
                    .status("SUBMITTED")
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            MutationRequest saved = mutationRequestRepository.save(request);
            auditEventService.recordEvent(requester.getId(), "SUBMIT_MUTATION", "MUTATION", saved.getId(), "Type: " + mutationType);
            return saved;
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit mutation request", e);
        }
    }

    @Transactional
    public Map<String, Object> evaluateMutation(String mutationId) {
        MutationRequest mutation = mutationRequestRepository.findById(mutationId)
                .orElseThrow(() -> new IllegalArgumentException("Mutation request not found: " + mutationId));

        mutation.setStatus("EVALUATING");
        mutation.setUpdatedAt(Instant.now());

        // 1. Resolve Target Parcels
        List<String> parcelIds = parseJsonList(mutation.getTargetParcelIds());
        List<Parcel> targetParcels = parcelRepository.findAllById(parcelIds);

        // 2. Fetch Evidence
        List<EvidenceRecord> allEvidence = new ArrayList<>();
        for (Parcel p : targetParcels) {
            allEvidence.addAll(evidenceRecordRepository.findByParcelIdOrderByObservedAtDesc(p.getId()));
        }
        allEvidence.addAll(evidenceRecordRepository.findByRequestIdOrderByObservedAtDesc(mutationId));

        // 3. Run M5: Contradiction Reconciliation
        List<NormalizedAssertion> normalizedAssertions = contradictionEngine.normalizeAssertions(allEvidence);
        List<ConflictWitness> conflictWitnesses = contradictionEngine.detectContradictions(normalizedAssertions);

        // 4. Run M2: Parcel Lineage & Topology Verification
        LineageVerificationResult lineageResult = lineageVerifier.verifyMutationLineage(mutation, targetParcels);

        // 5. Run M8: Uncertainty-Aware Evaluation
        List<PredicateEvaluation> predicates = uncertaintyEngine.evaluatePredicates(
                mutation, targetParcels, allEvidence, conflictWitnesses,
                lineageResult.isValid(), lineageResult.getExplanation()
        );

        DecisionType decision = uncertaintyEngine.evaluateDecision(predicates);

        // Persist Predicate Results
        predicateResultRepository.deleteByMutationId(mutationId);
        for (PredicateEvaluation pe : predicates) {
            PredicateResult pr = PredicateResult.builder()
                    .id(UUID.randomUUID().toString())
                    .mutation(mutation)
                    .predicateCode(pe.getPredicateCode())
                    .status(pe.getStatus().name())
                    .explanation(pe.getExplanation())
                    .evidenceRefs(pe.getEvidenceRefs())
                    .evaluatedAt(Instant.now())
                    .build();
            predicateResultRepository.save(pr);
        }

        // Generate and Persist Decision Certificate (including evidence fingerprints)
        DecisionCertificate certificate = certificateEngine.generateCertificate(
                mutation, decision, predicates, conflictWitnesses, lineageResult, allEvidence
        );
        decisionCertificateRepository.findByMutationId(mutationId).ifPresent(decisionCertificateRepository::delete);
        decisionCertificateRepository.save(certificate);

        // Update Mutation Status based on Decision
        switch (decision) {
            case AUTHORIZE -> mutation.setStatus("READY_FOR_COMMIT");
            case HOLD -> {
                mutation.setStatus("HELD");
                mutation.setDecisionReason("Held due to unresolved predicates or missing evidence.");
            }
            case REJECT -> {
                mutation.setStatus("REJECTED");
                mutation.setDecisionReason("Rejected due to violated mandatory conditions or contradictions.");
            }
        }
        mutationRequestRepository.save(mutation);

        Map<String, Object> evalResult = new HashMap<>();
        evalResult.put("mutationId", mutationId);
        evalResult.put("decision", decision.name());
        evalResult.put("status", mutation.getStatus());
        evalResult.put("conflictWitnesses", conflictWitnesses);
        evalResult.put("predicates", predicates);
        evalResult.put("lineageResult", lineageResult);
        evalResult.put("certificateHash", certificate.getCertificateHash());

        return evalResult;
    }

    @Transactional
    public MutationRequest reviewMutation(String mutationId, String action, String reason, User reviewer) {
        MutationRequest mutation = mutationRequestRepository.findById(mutationId)
                .orElseThrow(() -> new IllegalArgumentException("Mutation request not found: " + mutationId));

        Optional<DecisionCertificate> certOpt = decisionCertificateRepository.findByMutationId(mutationId);

        if ("APPROVE".equalsIgnoreCase(action)) {
            // Strict Server-Side Gate: cannot approve a request evaluated as HOLD or REJECT
            if (certOpt.isPresent() && !"AUTHORIZE".equalsIgnoreCase(certOpt.get().getDecision())) {
                throw new IllegalStateException("Review action APPROVE rejected: mutation has unfulfilled predicates (evaluation decision: "
                        + certOpt.get().getDecision() + "). Settlement cannot be approved while on HOLD or REJECT.");
            }
            if ("HELD".equals(mutation.getStatus()) || "REJECTED".equals(mutation.getStatus())) {
                throw new IllegalStateException("Review action APPROVE rejected: mutation status is " + mutation.getStatus()
                        + ". Cannot approve without required evidence resolution.");
            }
            mutation.setStatus("READY_FOR_COMMIT");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            mutation.setStatus("REJECTED");
        } else if ("HOLD".equalsIgnoreCase(action) || "REQUEST_EVIDENCE".equalsIgnoreCase(action) || "ESCALATE".equalsIgnoreCase(action)) {
            mutation.setStatus("HELD");
        }
        mutation.setAssignedReviewer(reviewer);
        mutation.setDecisionReason(reason);
        mutation.setUpdatedAt(Instant.now());

        auditEventService.recordEvent(reviewer.getId(), "REVIEW_MUTATION", "MUTATION", mutationId, "Action: " + action + ", Reason: " + reason);
        return mutationRequestRepository.save(mutation);
    }

    @Transactional
    public Map<String, Object> commitMutationToBlockchain(String mutationId, User committer) {
        MutationRequest mutation = mutationRequestRepository.findById(mutationId)
                .orElseThrow(() -> new IllegalArgumentException("Mutation request not found: " + mutationId));

        if (!"READY_FOR_COMMIT".equals(mutation.getStatus())) {
            throw new IllegalStateException("Mutation is not in READY_FOR_COMMIT state (current: " + mutation.getStatus() + ")");
        }

        DecisionCertificate certificate = decisionCertificateRepository.findByMutationId(mutationId)
                .orElseThrow(() -> new IllegalStateException("Decision certificate not found for mutation " + mutationId));

        // Strict Server-Side Settlement Gate: enforce M8 outcome
        if (!"AUTHORIZE".equalsIgnoreCase(certificate.getDecision())) {
            throw new IllegalStateException("Settlement blocked by M8 uncertainty engine: decision is " + certificate.getDecision()
                    + ". Only AUTHORIZE mutations can be committed to the blockchain.");
        }

        List<String> parcelIds = parseJsonList(mutation.getTargetParcelIds());
        List<Parcel> targetParcels = parcelRepository.findAllById(parcelIds);

        // Atomic re-check of expected versions to prevent race conditions
        Map<String, Integer> expectedVersions = parseJsonMap(mutation.getExpectedVersions());
        for (Parcel p : targetParcels) {
            int expVer = expectedVersions.getOrDefault(p.getId(), p.getVersion());
            if (expVer != p.getVersion()) {
                mutation.setStatus("COMMIT_FAILED");
                mutation.setDecisionReason(String.format("Atomic check failed: parcel %s active version (v%d) != expected (v%d)",
                        p.getParcelCode(), p.getVersion(), expVer));
                mutationRequestRepository.save(mutation);
                throw new IllegalStateException(mutation.getDecisionReason());
            }
        }

        // Dispatch On-Chain Transaction via Web3j
        BlockchainCommitResult commitResult = dispatchOnChainCommit(mutation, targetParcels, certificate.getCertificateHash());

        if (!commitResult.isSuccess()) {
            mutation.setStatus("COMMIT_FAILED");
            mutation.setDecisionReason("On-chain transaction failed/reverted: " + commitResult.getErrorMessage());
            mutationRequestRepository.save(mutation);
            throw new RuntimeException(mutation.getDecisionReason());
        }

        // Persist Blockchain Transaction Proof
        BlockchainTransaction btx = BlockchainTransaction.builder()
                .id(UUID.randomUUID().toString())
                .mutation(mutation)
                .txHash(commitResult.getTxHash())
                .chainId(31337L)
                .contractAddress(blockchainService.getContractAddress())
                .blockNumber(commitResult.getBlockNumber())
                .receiptStatus(1)
                .gasUsed(commitResult.getGasUsed())
                .isSimulated(commitResult.isSimulated())
                .createdAt(Instant.now())
                .build();
        blockchainTransactionRepository.save(btx);

        // Apply Off-Chain State Projection Updates
        applyStateProjectionUpdates(mutation, targetParcels, commitResult.getTxHash());

        mutation.setStatus("COMMITTED");
        mutation.setUpdatedAt(Instant.now());
        mutationRequestRepository.save(mutation);

        auditEventService.recordEvent(committer.getId(), "COMMIT_MUTATION", "MUTATION", mutationId, "TxHash: " + commitResult.getTxHash() + (commitResult.isSimulated() ? " [SIMULATED]" : " [LIVE]"));

        Map<String, Object> resp = new HashMap<>();
        resp.put("mutationId", mutationId);
        resp.put("status", "COMMITTED");
        resp.put("txHash", commitResult.getTxHash());
        resp.put("blockNumber", commitResult.getBlockNumber());
        resp.put("certificateHash", certificate.getCertificateHash());
        resp.put("isSimulated", commitResult.isSimulated());
        return resp;
    }

    private BlockchainCommitResult dispatchOnChainCommit(MutationRequest mutation, List<Parcel> targetParcels, String certDigest) {
        String type = mutation.getMutationType();

        switch (type) {
            case "REGISTRATION" -> {
                try {
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    String parcelCode = payload.get("parcelCode").asText();
                    String ownerAddr = mutation.getRequestedBy().getWalletAddress();
                    return blockchainService.registerParcelOnChain(parcelCode, ownerAddr, certDigest);
                } catch (Exception e) {
                    throw new RuntimeException("Registration dispatch error", e);
                }
            }
            case "TRANSFER" -> {
                try {
                    Parcel p = targetParcels.get(0);
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    String buyerId = payload.get("buyerUserId").asText();
                    User buyer = userRepository.findById(buyerId)
                            .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + buyerId));
                    return blockchainService.transferOwnershipOnChain(p.getParcelCode(), buyer.getWalletAddress(), p.getVersion(), certDigest);
                } catch (Exception e) {
                    throw new RuntimeException("Transfer dispatch error", e);
                }
            }
            case "SPLIT" -> {
                try {
                    Parcel parent = targetParcels.get(0);
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    JsonNode childrenNode = payload.get("children");

                    List<String> childCodes = new ArrayList<>();
                    List<String> childOwners = new ArrayList<>();
                    for (JsonNode child : childrenNode) {
                        childCodes.add(child.get("parcelCode").asText());
                        String ownerId = child.has("ownerUserId") ? child.get("ownerUserId").asText() : mutation.getRequestedBy().getId();
                        User u = userRepository.findById(ownerId).orElse(mutation.getRequestedBy());
                        childOwners.add(u.getWalletAddress());
                    }

                    return blockchainService.splitParcelOnChain(parent.getParcelCode(), parent.getVersion(), childCodes, childOwners, certDigest);
                } catch (Exception e) {
                    throw new RuntimeException("Split dispatch error", e);
                }
            }
            case "MERGE" -> {
                try {
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    String childCode = payload.get("mergedParcelCode").asText();
                    String ownerId = payload.has("ownerUserId") ? payload.get("ownerUserId").asText() : mutation.getRequestedBy().getId();
                    User childOwner = userRepository.findById(ownerId).orElse(mutation.getRequestedBy());

                    List<String> parentCodes = targetParcels.stream().map(Parcel::getParcelCode).toList();
                    List<Integer> parentVersions = targetParcels.stream().map(Parcel::getVersion).toList();

                    return blockchainService.mergeParcelsOnChain(parentCodes, parentVersions, childCode, childOwner.getWalletAddress(), certDigest);
                } catch (Exception e) {
                    throw new RuntimeException("Merge dispatch error", e);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported mutation type: " + type);
        }
    }

    private void applyStateProjectionUpdates(MutationRequest mutation, List<Parcel> targetParcels, String txHash) {
        String type = mutation.getMutationType();

        try {
            switch (type) {
                case "REGISTRATION" -> {
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    Parcel newParcel = Parcel.builder()
                            .id(UUID.randomUUID().toString())
                            .parcelCode(payload.get("parcelCode").asText())
                            .surveyNumber(payload.get("surveyNumber").asText())
                            .locality(payload.get("locality").asText())
                            .areaSqm(new BigDecimal(payload.get("areaSqm").asText()))
                            .geometryWkt(payload.get("geometryWkt").asText())
                            .status("ACTIVE")
                            .owner(mutation.getRequestedBy())
                            .version(1)
                            .blockchainTxHash(txHash)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    parcelRepository.save(newParcel);
                }
                case "TRANSFER" -> {
                    Parcel p = targetParcels.get(0);
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    String buyerId = payload.get("buyerUserId").asText();
                    User buyer = userRepository.findById(buyerId)
                            .orElseThrow(() -> new IllegalArgumentException("Buyer user not found"));
                    p.setOwner(buyer);
                    p.setVersion(p.getVersion() + 1);
                    p.setBlockchainTxHash(txHash);
                    p.setUpdatedAt(Instant.now());
                    parcelRepository.save(p);
                }
                case "SPLIT" -> {
                    Parcel parent = targetParcels.get(0);
                    parent.setStatus("INACTIVE_SPLIT");
                    parent.setVersion(parent.getVersion() + 1);
                    parent.setBlockchainTxHash(txHash);
                    parent.setUpdatedAt(Instant.now());
                    parcelRepository.save(parent);

                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    for (JsonNode childNode : payload.get("children")) {
                        String childOwnerId = childNode.has("ownerUserId") ? childNode.get("ownerUserId").asText() : mutation.getRequestedBy().getId();
                        User childOwner = userRepository.findById(childOwnerId).orElse(mutation.getRequestedBy());

                        Parcel child = Parcel.builder()
                                .id(UUID.randomUUID().toString())
                                .parcelCode(childNode.get("parcelCode").asText())
                                .surveyNumber(childNode.has("surveyNumber") ? childNode.get("surveyNumber").asText() : parent.getSurveyNumber() + "-S")
                                .locality(parent.getLocality())
                                .areaSqm(new BigDecimal(childNode.get("areaSqm").asText()))
                                .geometryWkt(childNode.get("geometryWkt").asText())
                                .status("ACTIVE")
                                .owner(childOwner)
                                .version(1)
                                .blockchainTxHash(txHash)
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                        Parcel savedChild = parcelRepository.save(child);

                        // Lineage edge
                        ParcelLineage edge = ParcelLineage.builder()
                                .id(UUID.randomUUID().toString())
                                .parentParcel(parent)
                                .childParcel(savedChild)
                                .mutation(mutation)
                                .relationType("SPLIT")
                                .createdAt(Instant.now())
                                .build();
                        parcelLineageRepository.save(edge);
                    }
                }
                case "MERGE" -> {
                    JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
                    String childOwnerId = payload.has("ownerUserId") ? payload.get("ownerUserId").asText() : mutation.getRequestedBy().getId();
                    User childOwner = userRepository.findById(childOwnerId).orElse(mutation.getRequestedBy());

                    // Deactivate parents
                    for (Parcel parent : targetParcels) {
                        parent.setStatus("INACTIVE_MERGED");
                        parent.setVersion(parent.getVersion() + 1);
                        parent.setBlockchainTxHash(txHash);
                        parent.setUpdatedAt(Instant.now());
                        parcelRepository.save(parent);
                    }

                    // Create merged child
                    Parcel mergedChild = Parcel.builder()
                            .id(UUID.randomUUID().toString())
                            .parcelCode(payload.get("mergedParcelCode").asText())
                            .surveyNumber(payload.has("surveyNumber") ? payload.get("surveyNumber").asText() : "SY-MERGED-" + System.currentTimeMillis())
                            .locality(targetParcels.get(0).getLocality())
                            .areaSqm(new BigDecimal(payload.get("mergedAreaSqm").asText()))
                            .geometryWkt(payload.get("mergedGeometryWkt").asText())
                            .status("ACTIVE")
                            .owner(childOwner)
                            .version(1)
                            .blockchainTxHash(txHash)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    Parcel savedMerged = parcelRepository.save(mergedChild);

                    // Lineage edges
                    for (Parcel parent : targetParcels) {
                        ParcelLineage edge = ParcelLineage.builder()
                                .id(UUID.randomUUID().toString())
                                .parentParcel(parent)
                                .childParcel(savedMerged)
                                .mutation(mutation)
                                .relationType("MERGE")
                                .createdAt(Instant.now())
                                .build();
                        parcelLineageRepository.save(edge);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to update database projection state", e);
            throw new RuntimeException("Projection update failure: " + e.getMessage(), e);
        }
    }

    private List<String> parseJsonList(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyList();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Integer> parseJsonMap(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyMap();
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
