// src/main/java/com/gymsystem/booking/CalendarController.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.CalendarItem;
import com.gymsystem.booking.config.BookingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** Read-only calendar API used by the frontend to render monthly views. */
@RestController
@RequestMapping("/api/v1/classes/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final ClassSessionRepository sessionRepo;
    private final BookingRepository bookingRepo;
    private final BookingPolicyRepository policyRepo;
    private final BookingConfigService bookingConfigService;

    @GetMapping
    public ResponseEntity<List<CalendarItem>> calendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(name = "type", required = false) String typeCode, // optional filter by class type
            @RequestParam(name = "onlyOpen", defaultValue = "false") boolean onlyOpen // <— NEW: returns only sessions open for booking
    ) {
        if (from == null || to == null || !from.isBefore(to)) {
            throw new IllegalArgumentException("Invalid date range");
        }

        // Load sessions ordered by startAt (with classType fetched in the query)
        var sessions = sessionRepo.findCalendar(from, to, typeCode);
        var now = Instant.now();
        var cfg = bookingConfigService.get();
        var policy = policyRepo.findTopByOrderByIdAsc().orElse(null);

        List<CalendarItem> items = new ArrayList<>(sessions.size());
        for (var s : sessions) {
            long booked = bookingRepo.countActiveBySessionId(s.getId());
            long spotsLeft = Math.max(0, s.getCapacity() - booked);

            // Compute monthly publish window open time for this session.
            // Example: if publish_days_before_month=15 and session is in November,
            // booking opens at (Nov-01 00:00:00Z - 15 days).
            var z = s.getStartAt().atZone(ZoneOffset.UTC);
            var firstDay = z.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            var openAt = firstDay.minusDays(cfg.getPublishDaysBeforeMonth());

            // Compute global policy horizon: now + openDaysInAdvance
            boolean withinHorizon = true;
            if (policy != null) {
                Instant horizon = now.plus(policy.getOpenDaysInAdvance(), ChronoUnit.DAYS);
                withinHorizon = !s.getStartAt().isAfter(horizon);
            }

            // Is booking allowed *right now* for this session?
            boolean openForBooking =
                    !s.isCanceled()
                    && now.isBefore(s.getStartAt())
                    && !ZonedDateTime.now(ZoneOffset.UTC).isBefore(openAt)
                    && withinHorizon;

            // If onlyOpen requested, skip sessions that are closed or full
            if (onlyOpen && (!openForBooking || spotsLeft <= 0)) {
                continue;
            }

            items.add(new CalendarItem(
                    s.getId(),
                    s.getClassType().getCode(),
                    s.getClassType().getName(),
                    s.getStartAt(),
                    s.getEndAt(),
                    s.getCapacity(),
                    booked,
                    spotsLeft,
                    s.isCanceled(),
                    openForBooking
            ));
        }
        return ResponseEntity.ok(items);
    }
}
