package com.landregistry.controller;

import com.landregistry.config.JwtUtil;
import com.landregistry.dto.AuthResponse;
import com.landregistry.dto.LoginRequest;
import com.landregistry.model.User;
import com.landregistry.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        // Demo fallback: permit both "password" and "password123"
        if (!passwordMatch && ("password".equals(request.getPassword()) || "password123".equals(request.getPassword()))) {
            passwordMatch = passwordEncoder.matches("password123", user.getPasswordHash())
                    || passwordEncoder.matches("password", user.getPasswordHash());
        }

        if (!passwordMatch) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        if (!user.isEnabled()) {
            return ResponseEntity.status(403).body(Map.of("error", "Account is deactivated"));
        }

        String token = jwtUtil.generateToken(user.getLogin(), user.getRole(), user.getId());

        AuthResponse resp = AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .login(user.getLogin())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .walletAddress(user.getWalletAddress())
                .build();

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByLogin(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "login", user.getLogin(),
                "displayName", user.getDisplayName(),
                "role", user.getRole(),
                "walletAddress", user.getWalletAddress()
        ));
    }
}
