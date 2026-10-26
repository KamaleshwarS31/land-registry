package com.landregistry.controller;

import com.landregistry.model.DecisionCertificate;
import com.landregistry.repository.DecisionCertificateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final DecisionCertificateRepository certificateRepository;

    public CertificateController(DecisionCertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<DecisionCertificate> getCertificate(@PathVariable String id) {
        // Try by mutationId or certificate id
        DecisionCertificate cert = certificateRepository.findByMutationId(id)
                .orElseGet(() -> certificateRepository.findById(id).orElse(null));

        if (cert == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cert);
    }
}
