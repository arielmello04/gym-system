// src/main/java/com/gymsystem/security/SecurityConfig.java
package com.gymsystem.security;

import com.gymsystem.security.jwt.JwtAuthFilter; // Imports our custom JWT filter
import lombok.RequiredArgsConstructor; // Imports Lombok annotation for constructor injection
import org.springframework.context.annotation.Bean; // Imports Bean annotation
import org.springframework.context.annotation.Configuration; // Imports Configuration annotation
import org.springframework.http.HttpMethod; // Imports HttpMethod enum for request method matching
import org.springframework.security.authentication.AuthenticationManager; // AuthenticationManager interface
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Provides AuthenticationManager from config
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Enables method-level security
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // HTTP security DSL
import org.springframework.security.config.http.SessionCreationPolicy; // Controls session creation policy
import org.springframework.security.web.SecurityFilterChain; // Represents the security filter chain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // Base filter for username/password auth

/**
 * Security configuration defining public and protected endpoints and JWT filter wiring.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter; // Injects our custom JWT authentication filter

    /**
     * Exposes AuthenticationManager to be used in services.
     * @param authConfig the authentication configuration provided by Spring
     * @return the configured AuthenticationManager
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager(); // Delegates to Spring's provided manager
    }

    /**
     * Builds the security filter chain specifying which endpoints are public and the stateless session policy.
     * @param http HttpSecurity builder
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---- public docs (Swagger/OpenAPI) ----
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // ---- auth & health ----
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,  "/actuator/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/checkin/callback").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/payments/callback/**").permitAll()
                // ---- PUBLIC: classes catalog & calendar ----
                .requestMatchers(HttpMethod.GET, "/api/v1/classes/types").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/classes/calendar").permitAll()
                // ---- PUBLIC: static resources ----
                .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                // ---- everything else requires auth ----
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
