// src/main/java/com/gymsystem/auth/AuthController.java
package com.gymsystem.auth;

import com.gymsystem.auth.dto.AuthResponse; // DTO for response
import com.gymsystem.auth.dto.LoginRequest; // DTO for login
import com.gymsystem.auth.dto.SignupRequest; // DTO for signup
import jakarta.validation.Valid; // Annotation to trigger bean validation
import lombok.RequiredArgsConstructor; // Lombok for constructor injection
import org.springframework.http.ResponseEntity; // HTTP response helper
import org.springframework.web.bind.annotation.*; // REST annotations

/**
 * REST endpoints for authentication flows (signup and login).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user given a valid admin-issued signup token.
     * @param request the validated signup payload
     * @return an AuthResponse containing a Bearer token
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        String token = authService.signup(request); // Performs sign-up and retrieves JWT
        return ResponseEntity.ok(new AuthResponse(token, "Bearer")); // Wraps token in a response DTO
    }

    /**
     * Logs in an existing user with email and password.
     * @param request the validated login payload
     * @return an AuthResponse containing a Bearer token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request); // Authenticates and retrieves JWT
        return ResponseEntity.ok(new AuthResponse(token, "Bearer")); // Returns token response
    }
}
