// src/main/java/com/gymsystem/booking/dto/BookingPolicyResponse.java
package com.gymsystem.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

/**
 * Response payload representing the current booking policy.
 */
@Data
@AllArgsConstructor
public class BookingPolicyResponse {

    private int openDaysInAdvance; // Number of days ahead users can book

    private Instant createdAt; // When the policy was created

    private Instant updatedAt; // When the policy was last updated
}
