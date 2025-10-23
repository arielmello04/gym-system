// src/main/java/com/gymsystem/booking/config/BookingConfigService.java
package com.gymsystem.booking.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BookingConfigService {

    private final BookingConfigRepository repo;

    public BookingConfig get() {
        return repo.findById(Boolean.TRUE).orElseThrow(() -> new IllegalStateException("Booking config missing"));
    }

    @Transactional
    public BookingConfig update(BookingConfig updated) {
        BookingConfig current = get();
        current.setPublishDaysBeforeMonth(updated.getPublishDaysBeforeMonth());
        current.setBusinessDays(updated.getBusinessDays());
        current.setBusinessStart(updated.getBusinessStart());
        current.setBusinessEnd(updated.getBusinessEnd());
        
        current.setCancelCutoffHours(updated.getCancelCutoffHours());
        current.setOnePerDayPerType(updated.isOnePerDayPerType());

        current.setUpdatedAt(Instant.now());
        return repo.save(current);
    }
}
