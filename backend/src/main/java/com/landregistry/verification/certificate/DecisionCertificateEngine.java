package com.landregistry.verification.certificate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.landregistry.model.DecisionCertificate;
import com.landregistry.model.MutationRequest;
import com.landregistry.verification.m2.LineageVerificationResult;
import com.landregistry.verification.m5.ConflictWitness;
import com.landregistry.verification.m8.DecisionType;
import com.landregistry.verification.m8.PredicateEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class DecisionCertificateEngine {

    private static final Logger log = LoggerFactory.getLogger(DecisionCertificateEngine.class);

    private final ObjectMapper sortedMapper;

    public DecisionCertificateEngine() {
        this.sortedMapper = new ObjectMapper();
        this.sortedMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public DecisionCertificate generateCertificate(
            MutationRequest mutation,
            DecisionType decision,
            List<PredicateEvaluation> predicates,
            List<ConflictWitness> conflicts,
            LineageVerificationResult lineageResult
    ) {
        return generateCertificate(mutation, decision, predicates, conflicts, lineageResult, Collections.emptyList());
    }

    public DecisionCertificate generateCertificate(
            MutationRequest mutation,
            DecisionType decision,
            List<PredicateEvaluation> predicates,
            List<ConflictWitness> conflicts,
            LineageVerificationResult lineageResult,
            List<com.landregistry.model.EvidenceRecord> evidenceRecords
    ) {
        try {
            Map<String, Object> canonicalMap = new TreeMap<>();
            canonicalMap.put("certificateVersion", "1.0");
            canonicalMap.put("mutationId", mutation.getId());
            canonicalMap.put("mutationType", mutation.getMutationType());
            canonicalMap.put("targetParcelIds", mutation.getTargetParcelIds());
            canonicalMap.put("expectedVersions", mutation.getExpectedVersions());
            canonicalMap.put("requestedBy", mutation.getRequestedBy() != null ? mutation.getRequestedBy().getId() : null);
            canonicalMap.put("decision", decision.name());

            // Evidence fingerprints
            List<String> evidenceHashes = (evidenceRecords != null)
                    ? evidenceRecords.stream()
                        .map(com.landregistry.model.EvidenceRecord::getSha256Hash)
                        .filter(Objects::nonNull)
                        .sorted()
                        .toList()
                    : Collections.emptyList();
            canonicalMap.put("evidenceHashes", evidenceHashes);

            // Predicate summary
            Map<String, String> predMap = new TreeMap<>();
            for (PredicateEvaluation pe : predicates) {
                predMap.put(pe.getPredicateCode(), pe.getStatus().name() + ": " + pe.getExplanation());
            }
            canonicalMap.put("predicateResults", predMap);

            // Conflict count & codes
            List<String> conflictCodes = conflicts.stream().map(ConflictWitness::getRuleCode).sorted().toList();
            canonicalMap.put("conflictCodes", conflictCodes);

            // Lineage status
            canonicalMap.put("lineageValid", lineageResult.isValid());
            canonicalMap.put("lineageExplanation", lineageResult.getExplanation());

            // Timestamp in ISO-8601 UTC
            String timestamp = Instant.now().toString();
            canonicalMap.put("issuedAt", timestamp);

            // Canonical JSON string
            String canonicalJson = sortedMapper.writeValueAsString(canonicalMap);

            // SHA-256 Digest
            String sha256Hex = computeDigest(canonicalJson);

            return DecisionCertificate.builder()
                    .id(UUID.randomUUID().toString())
                    .mutation(mutation)
                    .decision(decision.name())
                    .canonicalPayload(canonicalJson)
                    .certificateHash(sha256Hex)
                    .createdAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate decision certificate", e);
            throw new RuntimeException("Decision certificate generation failed", e);
        }
    }

    public boolean verifyDigest(String canonicalJson, String expectedHash) {
        if (canonicalJson == null || expectedHash == null) return false;
        String recomputed = computeDigest(canonicalJson);
        return recomputed.equalsIgnoreCase(expectedHash);
    }

    public String computeDigest(String canonicalJson) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Digest computation failed", e);
        }
    }
}
