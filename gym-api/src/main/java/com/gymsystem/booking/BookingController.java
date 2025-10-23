// src/main/java/com/gymsystem/booking/BookingController.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.AvailabilityItem; 
import com.gymsystem.booking.dto.BookingResponse; 
import lombok.RequiredArgsConstructor; 
import org.springframework.format.annotation.DateTimeFormat; 
import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.*; 

import java.time.Instant;
import java.util.List;

/**
 * User-facing endpoints for class availability and bookings.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService; // Injects the booking service

    /**
     * Lists available sessions between two ISO-8601 instants (inclusive).
     * Example: /api/v1/classes/availability?from=2025-01-01T00:00:00Z&to=2025-01-31T23:59:59Z
     * @param from the lower bound of the window (ISO-8601)
     * @param to the upper bound of the window (ISO-8601)
     * @return a list of availability items
     */
    @GetMapping("/classes/availability") // Maps to GET /api/v1/classes/availability
    public ResponseEntity<List<AvailabilityItem>> availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from, // Parses "from" param as Instant
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to // Parses "to" param as Instant
    ) {
        var items = bookingService.getAvailability(from, to); // Delegates to service
        return ResponseEntity.ok(items); // Returns 200 OK with the list
    }

    /**
     * Books a specific session for the current user.
     * @param sessionId the id of the session to be booked
     * @return a booking response with details
     */
    @PostMapping("/classes/{sessionId}/book") // Maps to POST /api/v1/classes/{sessionId}/book
    public ResponseEntity<BookingResponse> book(@PathVariable Long sessionId) { // Declares the book endpoint
        var response = bookingService.bookSession(sessionId); // Delegates to service
        return ResponseEntity.ok(response); // Returns 200 OK with booking data
    }

    /**
     * Cancels a booking that belongs to the current user (idempotent).
     * @param bookingId the id of the booking to cancel
     * @return 204 No Content on success
     */
    @DeleteMapping("/bookings/{bookingId}") // Maps to DELETE /api/v1/bookings/{bookingId}
    public ResponseEntity<Void> cancel(@PathVariable Long bookingId) {
        bookingService.cancelMyBooking(bookingId); // Delegates to service
        return ResponseEntity.noContent().build(); // Returns 204 No Content
    }
}
