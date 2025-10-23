// src/main/java/com/gymsystem/booking/config/BookingConfig.java
package com.gymsystem.booking.config;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "booking_config")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BookingConfig {

    /** Single-row table; id=true always. */
    @Id
    @Column(name = "id")
    private Boolean id;

    @Column(name = "publish_days_before_month", nullable = false)
    private int publishDaysBeforeMonth;

    @Column(name = "business_days", length = 32)
    private String businessDays;

    @Column(name = "business_start", nullable = false)
    private LocalTime businessStart;

    @Column(name = "business_end", nullable = false)
    private LocalTime businessEnd;

    @Column(name = "cancel_cutoff_hours", nullable = false)
    private int cancelCutoffHours;

    @Column(name = "one_per_day_per_type", nullable = false)
    private boolean onePerDayPerType;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
