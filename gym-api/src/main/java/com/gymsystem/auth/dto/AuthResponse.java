// src/main/java/com/gymsystem/auth/dto/AuthResponse.java
package com.gymsystem.auth.dto;

import lombok.AllArgsConstructor; // Generates all-args constructor
import lombok.Data; // Generates getters/setters/toString

/**
 * Response returned after successful sign-up or login.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken; // JWT access token
    private String tokenType; // Token type (always "Bearer")
}
