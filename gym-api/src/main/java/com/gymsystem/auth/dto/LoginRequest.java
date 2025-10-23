// src/main/java/com/gymsystem/auth/dto/LoginRequest.java
package com.gymsystem.auth.dto;

import jakarta.validation.constraints.Email; // Email validation
import jakarta.validation.constraints.NotBlank; // NotBlank validation
import lombok.Data; // Lombok to generate boilerplate

/**
 * Payload for performing a login with email and password.
 */
@Data // Lombok annotation
public class LoginRequest { // Declares LoginRequest DTO

    @Email // Validates email
    @NotBlank // Ensures not blank
    private String email; // User email

    @NotBlank // Ensures not blank
    private String password; // Raw password to verify
} // Ends LoginRequest
