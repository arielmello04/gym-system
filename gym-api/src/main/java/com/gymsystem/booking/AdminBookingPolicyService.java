// src/main/java/com/gymsystem/booking/AdminBookingPolicyService.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.AdminUpdatePolicyRequest;
import com.gymsystem.booking.dto.BookingPolicyResponse;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Business logic for reading and updating the global booking policy.
 */
@Service
@RequiredArgsConstructor
public class AdminBookingPolicyService {

    private final BookingPolicyRepository policyRepository; // Repository to load/save policy
    private final UserRepository userRepository; // Repository to resolve the current admin

    /**
     * Retrieves the current policy (if none exists, creates a default one).
     * @return a response DTO with the policy data
     */
    @Transactional
    public BookingPolicyResponse getPolicy() {
        BookingPolicy policy = policyRepository.findTopByOrderByIdAsc() // Attempts to find existing policy
                .orElseGet(() -> createDefaultPolicy()); // Creates default if none is present
        return new BookingPolicyResponse( // Builds a response DTO
                policy.getOpenDaysInAdvance(), // Maps openDaysInAdvance
                policy.getCreatedAt(), // Maps createdAt
                policy.getUpdatedAt() // Maps updatedAt
        );
    }

    /**
     * Updates or creates the policy with the provided parameters.
     * @param request payload containing the desired openDaysInAdvance
     * @return a response DTO representing the updated policy
     */
    @Transactional
    public BookingPolicyResponse updatePolicy(AdminUpdatePolicyRequest request) {
        var now = Instant.now(); // Captures current timestamp
        var admin = getCurrentUser(); // Resolves the current authenticated admin

        BookingPolicy policy = policyRepository.findTopByOrderByIdAsc() // Finds existing policy if present
                .orElseGet(() -> BookingPolicy.builder() // Otherwise starts building a new one
                        .createdAt(now) // Sets createdAt for the new row
                        .createdByAdminId(admin.getId()) // Audits creator admin id
                        .openDaysInAdvance(request.getOpenDaysInAdvance()) // Sets initial value
                        .updatedAt(now) // Sets updatedAt same as createdAt
                        .build()); // Finishes building the new entity

        // If a policy already exists, simply update the fields
        policy.setOpenDaysInAdvance(request.getOpenDaysInAdvance()); // Updates the openDaysInAdvance field
        policy.setUpdatedAt(now); // Updates the updatedAt timestamp

        // If policy didn't exist before, createdByAdminId is set above. If it existed, we keep the original creator.
        var saved = policyRepository.save(policy); // Persists the policy (insert or update)
        return new BookingPolicyResponse( // Builds and returns a response DTO
                saved.getOpenDaysInAdvance(), // Maps openDaysInAdvance
                saved.getCreatedAt(), // Maps createdAt
                saved.getUpdatedAt() // Maps updatedAt
        );
    }

    /**
     * Creates and persists a default policy (15 days).
     * @return a persisted default policy entity
     */
    private BookingPolicy createDefaultPolicy() {
        var now = Instant.now(); // Gets current time
        var policy = BookingPolicy.builder() // Starts building a new BookingPolicy
                .openDaysInAdvance(15) // Uses a sensible default value
                .createdByAdminId(0L) // Uses 0L as neutral creator since no admin context here
                .createdAt(now) // Sets creation timestamp
                .updatedAt(now) // Sets update timestamp
                .build(); // Finishes building
        return policyRepository.save(policy); // Persists and returns the default policy
    }

    /**
     * Resolves the current authenticated user (admin) from the security context.
     * @return the User entity for the current principal
     */
    private User getCurrentUser() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName(); // Extracts principal email
        return userRepository.findByEmail(email) // Loads the user by email
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email)); // Throws if missing
    }
}
