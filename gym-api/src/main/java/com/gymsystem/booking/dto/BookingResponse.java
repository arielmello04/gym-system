// src/main/java/com/gymsystem/booking/dto/BookingResponse.java
package com.gymsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

/**
 * Response payload representing a booking record.
 */
@Data
@AllArgsConstructor
public class BookingResponse {

    private Long id; // Booking identifier

    private Long sessionId; // The session that was booked

    private String status; // The current status (BOOKED or CANCELED)

    private Instant createdAt; // When the booking was created

    private Instant canceledAt; // When the booking was canceled (if null, still active)
}
