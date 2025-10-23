// src/main/java/com/gymsystem/security/jwt/JwtAuthFilter.java
package com.gymsystem.security.jwt;

import com.gymsystem.security.userdetails.CustomUserDetailsService; 
import io.jsonwebtoken.Claims; 
import jakarta.servlet.FilterChain; 
import jakarta.servlet.ServletException; 
import jakarta.servlet.http.HttpServletRequest; 
import jakarta.servlet.http.HttpServletResponse; 
import lombok.RequiredArgsConstructor; 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; 
import org.springframework.stereotype.Component; 
import org.springframework.web.filter.OncePerRequestFilter; 
import java.io.IOException; 

/**
 * A filter that extracts a JWT from the Authorization header, validates it,
 * and sets the authentication in the security context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // Service that validates and parses JWTs
    private final CustomUserDetailsService userDetailsService; // Loads user details from DB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization"); // Reads Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { // Checks if header is missing or not Bearer
            filterChain.doFilter(request, response); // Continues filter chain without authentication
            return;
        }

        String token = authHeader.substring(7); // Extracts token by removing "Bearer " prefix
        Claims claims = jwtService.validateAndParseClaims(token); // Validates and parses JWT claims
        if (claims != null) {
            String email = claims.getSubject();
            var userDetails = userDetailsService.loadUserByUsername(email);
            var authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities() // Granted authorities from roles
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // Adds request details
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response); // Proceeds with the filter chain
    }
}
