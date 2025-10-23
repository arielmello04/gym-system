// src/main/java/com/gymsystem/booking/dto/AvailabilityItem.java
package com.gymsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

/**
 * A flat, client-friendly view of an available class session.
 */
@Data
@AllArgsConstructor
public class AvailabilityItem {

    private Long sessionId; // Unique identifier of the session

    private String classTypeCode; // Code of the class type (e.g., "PILATES")

    private String classTypeName; // Display name of the class type (e.g., "Pilates")

    private Instant startAt; // Start time of the session (UTC)

    private Instant endAt; // End time of the session (UTC)

    private int capacity; // Maximum allowed bookings for the session

    private long spotsLeft; // Number of remaining spots (capacity - booked count)

    private String notes; // Optional notes (may be null)
}
