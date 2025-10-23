// src/main/java/com/gymsystem/booking/AdminBookingPolicyController.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.AdminUpdatePolicyRequest;
import com.gymsystem.booking.dto.BookingPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints to read and update the global booking policy.
 */
@RestController
@RequestMapping("/api/v1/admin/booking-policy")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminBookingPolicyController {

    private final AdminBookingPolicyService service;

    /**
     * Retrieves the current booking policy.
     * @return the current policy as a response DTO
     */
    @GetMapping
    public ResponseEntity<BookingPolicyResponse> get() {
        return ResponseEntity.ok(service.getPolicy()); // Returns 200 OK with the policy data
    }

    /**
     * Updates the booking policy.
     * @param request the validated request payload
     * @return the updated policy as a response DTO
     */
    @PutMapping
    public ResponseEntity<BookingPolicyResponse> update(@Valid @RequestBody AdminUpdatePolicyRequest request) {
        return ResponseEntity.ok(service.updatePolicy(request)); // Returns 200 OK with updated data
    }
}
