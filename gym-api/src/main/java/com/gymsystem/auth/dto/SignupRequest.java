    // src/main/java/com/gymsystem/auth/dto/SignupRequest.java
    package com.gymsystem.auth.dto;

    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import lombok.Data;

    /** Payload for user sign-up controlled by an invite token. */
    @Data
    public class SignupRequest {

        @Email @NotBlank
        private String email;

        @NotBlank
        private String password;

        @NotBlank
        private String inviteToken; // Admin-provided token required to authorize signup
    }
