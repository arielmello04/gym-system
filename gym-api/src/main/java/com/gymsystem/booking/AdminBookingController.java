// src/main/java/com/gymsystem/booking/AdminBookingController.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.AdminCreateSessionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for managing class sessions (create and cancel).
 */
@RestController
@RequestMapping("/api/v1/admin/classes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminBookingController {

    private final BookingService bookingService;

    /**
     * Creates a new class session.
     * @param request the validated request body with session details
     * @return the id of the created session
     */
    @PostMapping("/sessions")
    public ResponseEntity<Long> createSession(@Valid @RequestBody AdminCreateSessionRequest request) {
        Long id = bookingService.createSession(request); // Delegates creation to the service
        return ResponseEntity.ok(id); // Returns 200 OK with the new session id
    }

    /**
     * Cancels an existing session if it has no active bookings.
     * @param sessionId the path variable representing session id
     * @return 204 No Content on success
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> cancelSession(@PathVariable Long sessionId) {
        bookingService.cancelSession(sessionId); // Delegates cancellation to the service
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }
}
