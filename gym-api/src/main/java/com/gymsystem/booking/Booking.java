// src/main/java/com/gymsystem/booking/Booking.java
package com.gymsystem.booking;

import com.gymsystem.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Represents a user's reservation for a specific class session.
 */
@Entity
@Table(name = "bookings")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for the booking

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ClassSession session; // The class session that was booked

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who made the booking

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status; // Current status of the booking

    @Column(name = "created_at", nullable = false)
    private Instant createdAt; // When the booking was created

    @Column(name = "canceled_at")
    private Instant canceledAt; // When the booking was canceled (if ever)
}
