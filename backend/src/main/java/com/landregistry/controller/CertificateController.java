package com.landregistry.controller;

import com.landregistry.model.DecisionCertificate;
import com.landregistry.repository.DecisionCertificateRepository;
import com.landregistry.verification.certificate.DecisionCertificateEngine;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final DecisionCertificateRepository certificateRepository;
    private final DecisionCertificateEngine certificateEngine;

    public CertificateController(DecisionCertificateRepository certificateRepository, DecisionCertificateEngine certificateEngine) {
        this.certificateRepository = certificateRepository;
        this.certificateEngine = certificateEngine;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DecisionCertificate> getCertificate(@PathVariable String id) {
        DecisionCertificate cert = findCert(id);
        if (cert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cert);
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyCertificate(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        DecisionCertificate cert = findCert(id);
        if (cert == null) {
            return ResponseEntity.notFound().build();
        }

        String payloadToVerify = (body != null && body.containsKey("payload"))
                ? body.get("payload")
                : cert.getCanonicalPayload();

        String computedHash = certificateEngine.computeDigest(payloadToVerify);
        boolean matches = computedHash.equalsIgnoreCase(cert.getCertificateHash());

        return ResponseEntity.ok(Map.of(
                "certificateId", cert.getId(),
                "expectedHash", cert.getCertificateHash(),
                "computedHash", computedHash,
                "matches", matches,
                "tampered", !matches,
                "verifiedAt", Instant.now().toString()
        ));
    }

    private DecisionCertificate findCert(String id) {
        return certificateRepository.findByMutationId(id)
                .orElseGet(() -> certificateRepository.findById(id).orElse(null));
    }
}
