// src/main/java/com/gymsystem/booking/BookingPolicy.java
package com.gymsystem.booking;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Represents a global booking policy that controls how far in advance users can book sessions.
 */
@Entity
@Table(name = "booking_policies")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "open_days_in_advance", nullable = false)
    private int openDaysInAdvance;

    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId; // Admin user id who created the policy

    @Column(name = "created_at", nullable = false)
    private Instant createdAt; // When the policy row was created

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt; // When the policy row was last updated
}
