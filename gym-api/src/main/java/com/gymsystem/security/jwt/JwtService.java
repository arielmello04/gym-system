// src/main/java/com/gymsystem/security/jwt/JwtService.java
package com.gymsystem.security.jwt;

import io.jsonwebtoken.Claims; // Represents JWT claims
import io.jsonwebtoken.Jwts; // Entry point for JWT operations
import io.jsonwebtoken.SignatureAlgorithm; // Enum for signing algorithms
import io.jsonwebtoken.security.Keys; // Utility to create secure keys
import jakarta.annotation.PostConstruct; // Lifecycle annotation to init bean
import org.springframework.beans.factory.annotation.Value; // Injects values from properties
import org.springframework.stereotype.Service; // Marks as service component

import javax.crypto.SecretKey; // SecretKey interface
import java.time.Instant; // Java time Instant
import java.util.Date; // Legacy Date used by JJWT

/**
 * Service responsible for creating and validating JWT tokens.
 */
@Service
public class JwtService {

    @Value("${security.jwt.secret}") // Injects the secret from configuration
    private String secret; // Holds the configured secret string

    @Value("${security.jwt.expiration-seconds}") // Injects expiration from configuration
    private long expirationSeconds; // Holds the token expiration in seconds

    private SecretKey key; // Holds the computed SecretKey used for signing

    @PostConstruct // Called by Spring after dependency injection
    void init() { // Initializes the SecretKey
        this.key = Keys.hmacShaKeyFor(secret.getBytes()); // Builds an HMAC key from the secret bytes
    }

    /**
     * Generates a JWT for a given subject (user email) and role.
     * @param subject the user identifier (email)
     * @param role the user's primary role
     * @return a signed JWT string
     */
    public String generateToken(String subject, String role) {
        Instant now = Instant.now(); // Captures current time
        Instant expiry = now.plusSeconds(expirationSeconds); // Computes expiration time
        return Jwts.builder() // Starts building the JWT
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("role", role)
                .signWith(key, SignatureAlgorithm.HS256) // Signs using our secret key
                .compact(); // Builds and compacts into the final token string
    }

    /**
     * Validates a JWT and returns its claims if valid.
     * @param token the raw JWT string
     * @return the parsed Claims or null if invalid
     */
    public Claims validateAndParseClaims(String token) {
        try {
            return Jwts.parserBuilder() // Creates a parser builder
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token) // Parses and validates the token
                    .getBody();
        } catch (Exception e) { // Catches any parsing or validation exception
            return null;
        }
    }
}
