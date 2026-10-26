package com.landregistry.verification.m2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.landregistry.model.MutationRequest;
import com.landregistry.model.Parcel;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ParcelLineageVerifier {

    private static final Logger log = LoggerFactory.getLogger(ParcelLineageVerifier.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WKTReader wktReader = new WKTReader();

    public LineageVerificationResult verifyMutationLineage(MutationRequest mutation, List<Parcel> targetParcels) {
        String type = mutation.getMutationType();

        try {
            switch (type) {
                case "TRANSFER" -> {
                    return verifyTransfer(mutation, targetParcels);
                }
                case "SPLIT" -> {
                    return verifySplit(mutation, targetParcels);
                }
                case "MERGE" -> {
                    return verifyMerge(mutation, targetParcels);
                }
                case "REGISTRATION" -> {
                    return verifyRegistration(mutation);
                }
                default -> {
                    return LineageVerificationResult.builder()
                            .valid(false)
                            .explanation("Unknown mutation type: " + type)
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Lineage verification error", e);
            return LineageVerificationResult.builder()
                    .valid(false)
                    .explanation("Lineage verification failed with error: " + e.getMessage())
                    .build();
        }
    }

    private LineageVerificationResult verifyRegistration(MutationRequest mutation) {
        try {
            JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
            if (!payload.has("geometryWkt") || payload.get("geometryWkt").asText().isBlank()) {
                return LineageVerificationResult.builder()
                        .valid(false)
                        .explanation("Registration requires a valid WKT polygon geometry.")
                        .build();
            }
            String wkt = payload.get("geometryWkt").asText();
            Geometry geom = wktReader.read(wkt);
            if (!geom.isValid()) {
                return LineageVerificationResult.builder()
                        .valid(false)
                        .explanation("Geometry is invalid (self-intersecting or non-closed polygon).")
                        .build();
            }
            return LineageVerificationResult.builder()
                    .valid(true)
                    .explanation("New parcel geometry is valid and topologically closed.")
                    .build();
        } catch (Exception e) {
            return LineageVerificationResult.builder()
                    .valid(false)
                    .explanation("Failed to parse registration geometry: " + e.getMessage())
                    .build();
        }
    }

    private LineageVerificationResult verifyTransfer(MutationRequest mutation, List<Parcel> targetParcels) {
        if (targetParcels.isEmpty()) {
            return LineageVerificationResult.builder().valid(false).explanation("Target parcel not found.").build();
        }
        Parcel p = targetParcels.get(0);
        if (!"ACTIVE".equals(p.getStatus())) {
            return LineageVerificationResult.builder().valid(false).explanation("Target parcel is not ACTIVE (current status: " + p.getStatus() + ").").build();
        }

        // Verify expected version
        int expectedVersion = extractExpectedVersion(mutation.getExpectedVersions(), p.getId());
        if (expectedVersion != p.getVersion()) {
            return LineageVerificationResult.builder()
                    .valid(false)
                    .explanation(String.format("Stale parcel version: mutation expects v%d, but active state is v%d.", expectedVersion, p.getVersion()))
                    .build();
        }

        return LineageVerificationResult.builder()
                .valid(true)
                .explanation("Transfer lineage and version invariant satisfied.")
                .build();
    }

    private LineageVerificationResult verifySplit(MutationRequest mutation, List<Parcel> targetParcels) {
        if (targetParcels.isEmpty()) {
            return LineageVerificationResult.builder().valid(false).explanation("Parent parcel not found.").build();
        }
        Parcel parent = targetParcels.get(0);
        if (!"ACTIVE".equals(parent.getStatus())) {
            return LineageVerificationResult.builder().valid(false).explanation("Parent parcel is not ACTIVE.").build();
        }

        int expectedVersion = extractExpectedVersion(mutation.getExpectedVersions(), parent.getId());
        if (expectedVersion != parent.getVersion()) {
            return LineageVerificationResult.builder()
                    .valid(false)
                    .explanation(String.format("Stale parent version: expected v%d, current is v%d.", expectedVersion, parent.getVersion()))
                    .build();
        }

        try {
            JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
            JsonNode childrenNode = payload.get("children");
            if (childrenNode == null || !childrenNode.isArray() || childrenNode.size() < 2) {
                return LineageVerificationResult.builder().valid(false).explanation("Split requires at least 2 child parcel definitions.").build();
            }

            Geometry parentGeom = wktReader.read(parent.getGeometryWkt());
            double parentArea = parent.getAreaSqm().doubleValue();

            List<Geometry> childGeoms = new ArrayList<>();
            double childAreaSum = 0;

            for (JsonNode child : childrenNode) {
                String childWkt = child.get("geometryWkt").asText();
                Geometry cg = wktReader.read(childWkt);
                if (!cg.isValid()) {
                    return LineageVerificationResult.builder().valid(false).explanation("Child parcel geometry is invalid.").build();
                }
                childGeoms.add(cg);
                if (child.has("areaSqm")) {
                    childAreaSum += child.get("areaSqm").asDouble();
                } else {
                    childAreaSum += cg.getArea();
                }
            }

            // Invariant 1: Check for overlaps between children
            for (int i = 0; i < childGeoms.size(); i++) {
                for (int j = i + 1; j < childGeoms.size(); j++) {
                    Geometry intersection = childGeoms.get(i).intersection(childGeoms.get(j));
                    if (intersection.getArea() > 0.01) { // non-trivial overlap
                        return LineageVerificationResult.builder()
                                .valid(false)
                                .explanation(String.format("Child parcels overlap topologically (intersection area: %.2f sqm).", intersection.getArea()))
                                .build();
                    }
                }
            }

            // Invariant 2: Area conservation within 0.1% tolerance
            double areaDiff = Math.abs(parentArea - childAreaSum);
            double tolerance = parentArea * 0.001; // 0.1%
            if (areaDiff > Math.max(tolerance, 1.0)) { // at least 1 sqm or 0.1%
                return LineageVerificationResult.builder()
                        .valid(false)
                        .areaDiscrepancySqm(areaDiff)
                        .explanation(String.format("Area conservation violated: parent has %.2f sqm, children sum to %.2f sqm (discrepancy: %.2f sqm exceeds tolerance).",
                                parentArea, childAreaSum, areaDiff))
                        .build();
            }

            return LineageVerificationResult.builder()
                    .valid(true)
                    .areaDiscrepancySqm(areaDiff)
                    .explanation(String.format("Split topological invariants passed: %d non-overlapping children conserve parent area (delta: %.4f sqm).",
                            childGeoms.size(), areaDiff))
                    .build();

        } catch (Exception e) {
            return LineageVerificationResult.builder().valid(false).explanation("Failed to verify split geometry: " + e.getMessage()).build();
        }
    }

    private LineageVerificationResult verifyMerge(MutationRequest mutation, List<Parcel> targetParcels) {
        if (targetParcels.size() < 2) {
            return LineageVerificationResult.builder().valid(false).explanation("Merge requires at least 2 active parent parcels.").build();
        }

        // Verify each parent is active and matches expected version
        for (Parcel p : targetParcels) {
            if (!"ACTIVE".equals(p.getStatus())) {
                return LineageVerificationResult.builder().valid(false).explanation("Parent parcel " + p.getParcelCode() + " is not ACTIVE.").build();
            }
            int expVer = extractExpectedVersion(mutation.getExpectedVersions(), p.getId());
            if (expVer != p.getVersion()) {
                return LineageVerificationResult.builder()
                        .valid(false)
                        .explanation(String.format("Stale version for parent %s: expected v%d, actual v%d.", p.getParcelCode(), expVer, p.getVersion()))
                        .build();
            }
        }

        try {
            JsonNode payload = objectMapper.readTree(mutation.getProposedPayload());
            String mergedWkt = payload.get("mergedGeometryWkt").asText();
            Geometry mergedGeom = wktReader.read(mergedWkt);
            if (!mergedGeom.isValid()) {
                return LineageVerificationResult.builder().valid(false).explanation("Merged parcel geometry is invalid.").build();
            }

            double parentsAreaSum = targetParcels.stream().mapToDouble(p -> p.getAreaSqm().doubleValue()).sum();
            double mergedArea = payload.has("mergedAreaSqm") ? payload.get("mergedAreaSqm").asDouble() : mergedGeom.getArea();

            double areaDiff = Math.abs(parentsAreaSum - mergedArea);
            double tolerance = parentsAreaSum * 0.001;
            if (areaDiff > Math.max(tolerance, 1.0)) {
                return LineageVerificationResult.builder()
                        .valid(false)
                        .areaDiscrepancySqm(areaDiff)
                        .explanation(String.format("Merge area conservation violated: parents sum to %.2f sqm, merged is %.2f sqm.",
                                parentsAreaSum, mergedArea))
                        .build();
            }

            return LineageVerificationResult.builder()
                    .valid(true)
                    .areaDiscrepancySqm(areaDiff)
                    .explanation(String.format("Merge lineage invariants verified across %d parents.", targetParcels.size()))
                    .build();

        } catch (Exception e) {
            return LineageVerificationResult.builder().valid(false).explanation("Failed to verify merge geometry: " + e.getMessage()).build();
        }
    }

    private int extractExpectedVersion(String expectedVersionsJson, String parcelId) {
        try {
            if (expectedVersionsJson != null && !expectedVersionsJson.isBlank()) {
                JsonNode node = objectMapper.readTree(expectedVersionsJson);
                if (node.has(parcelId)) {
                    return node.get(parcelId).asInt();
                }
            }
        } catch (Exception ignored) {}
        return 1;
    }
}
