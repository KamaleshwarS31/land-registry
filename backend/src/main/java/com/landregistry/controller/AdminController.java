package com.landregistry.controller;

import com.landregistry.blockchain.Web3jBlockchainService;
import com.landregistry.evidence.DocumentStorageService;
import com.landregistry.model.User;
import com.landregistry.repository.AuditEventRepository;
import com.landregistry.repository.UserRepository;
import com.landregistry.service.DemoResetService;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AuditEventRepository auditEventRepository;
    private final Web3jBlockchainService blockchainService;
    private final DocumentStorageService storageService;
    private final DemoResetService demoResetService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminController(
            UserRepository userRepository,
            AuditEventRepository auditEventRepository,
            Web3jBlockchainService blockchainService,
            DocumentStorageService storageService,
            DemoResetService demoResetService,
            PasswordEncoder passwordEncoder,
            Environment environment
    ) {
        this.userRepository = userRepository;
        this.auditEventRepository = auditEventRepository;
        this.blockchainService = blockchainService;
        this.storageService = storageService;
        this.demoResetService = demoResetService;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        health.put("activeProfiles", Arrays.asList(environment.getActiveProfiles()));
        health.put("storageProvider", storageService.getProviderName());
        health.put("blockchainMode", blockchainService.getMode());
        health.put("isSimulated", blockchainService.isSimulatedMode());
        health.put("blockchainConnected", blockchainService.isNodeConnected());
        health.put("contractAddress", blockchainService.getContractAddress());
        health.put("totalUsers", userRepository.count());
        health.put("recentAuditEvents", auditEventRepository.findTop100ByOrderByTimestampDesc());

        return ResponseEntity.ok(health);
    }

    @PostMapping("/demo-reset")
    public ResponseEntity<Map<String, Object>> resetDemoDataset() {
        demoResetService.resetDemoData();
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Demo dataset successfully reset to baseline scenarios (P-101 to P-405).",
                "resetAt", Instant.now().toString()
        ));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> userPayload) {
        String login = userPayload.get("login");
        if (userRepository.existsByLogin(login)) {
            return ResponseEntity.badRequest().build();
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .login(login)
                .passwordHash(passwordEncoder.encode(userPayload.getOrDefault("password", "password123")))
                .displayName(userPayload.getOrDefault("displayName", login))
                .role(userPayload.getOrDefault("role", "LANDOWNER"))
                .walletAddress(userPayload.getOrDefault("walletAddress", "0x0000000000000000000000000000000000000000"))
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        return ResponseEntity.ok(userRepository.save(user));
    }
}
