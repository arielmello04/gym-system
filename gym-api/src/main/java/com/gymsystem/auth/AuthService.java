// src/main/java/com/gymsystem/auth/AuthService.java
package com.gymsystem.auth;

import com.gymsystem.auth.dto.LoginRequest;
import com.gymsystem.auth.dto.SignupRequest;
import com.gymsystem.security.jwt.JwtService;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import com.gymsystem.user.UserRole;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Business logic for sign-up and login flows.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final com.gymsystem.auth.invite.SignupTokenService signupTokenService;

    /**
     * Registers a new user if the provided invite token is valid.
     * Validation and usage counting are handled by SignupTokenService.
     */
    @Transactional
    public String signup(SignupRequest request) {
        // Defensive: ensure email uniqueness early.
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Authorize signup by consuming the invite token atomically.
        signupTokenService.consumeOrThrow(request.getInviteToken());

        // Persist the new user with a strong one-way password hash.
        Instant now = Instant.now();
        String hashed = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(hashed)
                .role(UserRole.USER)   // Defaults to app user
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        userRepository.save(user);

        // Issue a JWT so the app can log the user in immediately.
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }

    /**
     * Authenticates a user and returns a new JWT on success.
     */
    public String login(LoginRequest request) {
        // Delegates credential validation to Spring Security.
        var authToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authenticationManager.authenticate(authToken);

        // Load user to embed role/claims in the token.
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return jwtService.generateToken(user.getEmail(), user.getRole().name());
    }
}
