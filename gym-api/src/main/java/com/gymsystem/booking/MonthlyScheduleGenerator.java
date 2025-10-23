// src/main/java/com/gymsystem/booking/MonthlyScheduleGenerator.java
package com.gymsystem.booking;

import com.gymsystem.booking.config.BookingConfigService;
import com.gymsystem.booking.dto.GenerateMonthRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Generates class sessions for a given month based on business hours and slot size. */
@Service
@RequiredArgsConstructor
public class MonthlyScheduleGenerator {

    private final BookingConfigService configService;
    private final ClassTypeRepository classTypeRepository;
    private final ClassSessionRepository classSessionRepository;

    /** Creates sessions for the specified month; skips days outside business days. */
    @Transactional
    public int generate(GenerateMonthRequest req, Long adminId) {
        var cfg = configService.get();
        var classType = classTypeRepository.findByCodeAndActiveTrue(req.getClassTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("Unknown or inactive class type: " + req.getClassTypeCode()));

        // Parse business days string (e.g., "MON-SAT") into a set for quick checks
        Set<DayOfWeek> businessDays = parseBusinessDays(cfg.getBusinessDays());

        // Build month boundaries in UTC
        LocalDate first = LocalDate.of(req.getYear(), req.getMonth(), 1);
        LocalDate last = first.withDayOfMonth(first.lengthOfMonth());

        int created = 0;
        for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) {
            if (!businessDays.contains(d.getDayOfWeek())) continue;

            // Compute start/end slots for the day in UTC
            ZonedDateTime dayStart = ZonedDateTime.of(d, cfg.getBusinessStart(), ZoneOffset.UTC);
            ZonedDateTime dayEnd   = ZonedDateTime.of(d, cfg.getBusinessEnd(), ZoneOffset.UTC);

            // Partition the day into slotMinutes and create sessions
            ZonedDateTime slotStart = dayStart;
            while (slotStart.isBefore(dayEnd)) {
                ZonedDateTime slotEnd = slotStart.plusMinutes(req.getSlotMinutes());
                if (slotEnd.isAfter(dayEnd)) break;

                var session = ClassSession.builder()
                        .classType(classType)
                        .startAt(slotStart.toInstant())
                        .endAt(slotEnd.toInstant())
                        .capacity(req.getCapacity())
                        .canceled(false)
                        .notes(null)
                        .createdByAdminId(adminId)
                        .createdAt(Instant.now())
                        .build();

                classSessionRepository.save(session);
                created++;
                slotStart = slotEnd;
            }
        }
        return created;
    }

    /** Parses strings like "MON-SAT" or "MON-FRI" or "MON,SAT" into a set of DayOfWeek. */
    private static final Map<String, DayOfWeek> ALIAS = Map.ofEntries(
            Map.entry("MON", DayOfWeek.MONDAY), Map.entry("MONDAY", DayOfWeek.MONDAY),
            Map.entry("TUE", DayOfWeek.TUESDAY), Map.entry("TUESDAY", DayOfWeek.TUESDAY),
            Map.entry("WED", DayOfWeek.WEDNESDAY), Map.entry("WEDNESDAY", DayOfWeek.WEDNESDAY),
            Map.entry("THU", DayOfWeek.THURSDAY), Map.entry("THURSDAY", DayOfWeek.THURSDAY),
            Map.entry("FRI", DayOfWeek.FRIDAY), Map.entry("FRIDAY", DayOfWeek.FRIDAY),
            Map.entry("SAT", DayOfWeek.SATURDAY), Map.entry("SATURDAY", DayOfWeek.SATURDAY),
            Map.entry("SUN", DayOfWeek.SUNDAY), Map.entry("SUNDAY", DayOfWeek.SUNDAY)
    );

    private Set<DayOfWeek> parseBusinessDays(String text) {
        var set = new HashSet<DayOfWeek>();
        String t = (text == null || text.isBlank()) ? "MON-SAT" : text.trim().toUpperCase();

        if (t.contains("-")) {
            String[] parts = t.split("-");
            DayOfWeek start = ALIAS.get(parts[0].trim());
            DayOfWeek end   = ALIAS.get(parts[1].trim());
            if (start == null || end == null) throw new IllegalArgumentException("Invalid business_days: " + text);
            DayOfWeek d = start;
            while (true) {
                set.add(d);
                if (d.equals(end)) break;
                d = DayOfWeek.of(d.getValue() % 7 + 1);
            }
        } else if (t.contains(",")) {
            for (String p : t.split(",")) {
                DayOfWeek d = ALIAS.get(p.trim());
                if (d == null) throw new IllegalArgumentException("Invalid business_days: " + text);
                set.add(d);
            }
        } else {
            DayOfWeek d = ALIAS.get(t);
            if (d == null) throw new IllegalArgumentException("Invalid business_days: " + text);
            set.add(d);
        }
        return set;
    }

}
