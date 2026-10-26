package com.landregistry.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthControllerTest {

    @Test
    void testPasswordMatches() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password123Hash = "$2a$10$Xxddeimd0ezFbUBQsrJnbOslDpx5XiQNqA3hKu5rkpv1kzgu5/AxG";
        assertTrue(encoder.matches("password123", password123Hash));
    }
}
