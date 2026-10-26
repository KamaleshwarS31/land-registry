package com.landregistry.controller;

import com.landregistry.dto.MutationReviewRequest;
import com.landregistry.dto.MutationSubmitRequest;
import com.landregistry.model.DecisionCertificate;
import com.landregistry.model.MutationRequest;
import com.landregistry.model.PredicateResult;
import com.landregistry.model.User;
import com.landregistry.repository.DecisionCertificateRepository;
import com.landregistry.repository.MutationRequestRepository;
import com.landregistry.repository.PredicateResultRepository;
import com.landregistry.repository.UserRepository;
import com.landregistry.service.MutationWorkflowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mutations")
public class MutationController {

    private final MutationWorkflowService mutationWorkflowService;
    private final MutationRequestRepository mutationRequestRepository;
    private final PredicateResultRepository predicateResultRepository;
    private final DecisionCertificateRepository decisionCertificateRepository;
    private final UserRepository userRepository;

    public MutationController(
            MutationWorkflowService mutationWorkflowService,
            MutationRequestRepository mutationRequestRepository,
            PredicateResultRepository predicateResultRepository,
            DecisionCertificateRepository decisionCertificateRepository,
            UserRepository userRepository
    ) {
        this.mutationWorkflowService = mutationWorkflowService;
        this.mutationRequestRepository = mutationRequestRepository;
        this.predicateResultRepository = predicateResultRepository;
        this.decisionCertificateRepository = decisionCertificateRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<MutationRequest>> getMutations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MutationRequest> list = mutationRequestRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<MutationRequest>> getMyRequests(Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(mutationRequestRepository.findByRequestedByIdOrderByCreatedAtDesc(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMutationById(@PathVariable String id) {
        MutationRequest mutation = mutationRequestRepository.findById(id).orElse(null);
        if (mutation == null) {
            return ResponseEntity.notFound().build();
        }

        List<PredicateResult> predicates = predicateResultRepository.findByMutationIdOrderByEvaluatedAtAsc(id);
        DecisionCertificate certificate = decisionCertificateRepository.findByMutationId(id).orElse(null);

        Map<String, Object> resp = new HashMap<>();
        resp.put("mutation", mutation);
        resp.put("predicates", predicates);
        resp.put("certificate", certificate);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/submit")
    public ResponseEntity<MutationRequest> submitMutation(
            @RequestBody MutationSubmitRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MutationRequest mutation = mutationWorkflowService.submitMutation(
                request.getMutationType(),
                request.getTargetParcelIds(),
                request.getExpectedVersions(),
                request.getProposedPayload(),
                user
        );

        return ResponseEntity.ok(mutation);
    }

    @PostMapping("/{id}/evaluate")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'SURVEY_REVIEWER', 'ADMIN')")
    public ResponseEntity<?> evaluateMutation(@PathVariable String id) {
        Map<String, Object> result = mutationWorkflowService.evaluateMutation(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'ADMIN')")
    public ResponseEntity<?> reviewMutation(
            @PathVariable String id,
            @RequestBody MutationReviewRequest request,
            Authentication authentication
    ) {
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MutationRequest updated = mutationWorkflowService.reviewMutation(id, request.getAction(), request.getReason(), user);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/commit")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'ADMIN')")
    public ResponseEntity<?> commitMutation(
            @PathVariable String id,
            Authentication authentication
    ) {
        User user = userRepository.findByLogin(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Object> result = mutationWorkflowService.commitMutationToBlockchain(id, user);
        return ResponseEntity.ok(result);
    }
}
