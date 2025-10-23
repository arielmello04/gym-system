// src/main/java/com/gymsystem/booking/BookingEnforcer.java
package com.gymsystem.booking;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Cancels user's future bookings when subscription becomes PAST_DUE (with optional grace). */
@Component
@RequiredArgsConstructor
public class BookingEnforcer {

    private final BookingRepository bookingRepository;

    @Value("${payments.past-due.cancel-future-bookings:true}")
    private boolean cancelFutureBookings;

    @Value("${payments.past-due.grace-hours:0}")
    private long graceHours;

    /** Returns number of canceled bookings. */
    @Transactional
    public int enforcePastDue(Long userId) {
        if (!cancelFutureBookings) return 0;
        Instant now = Instant.now();
        Instant cutoff = now.plusSeconds(graceHours * 3600);
        return bookingRepository.cancelFutureActiveByUser(userId, cutoff, now);
    }
}
