// src/main/java/com/gymsystem/booking/ClassSession.java
package com.gymsystem.booking;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Represents a scheduled class slot that users can book.
 */
@Entity
@Table(name = "class_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_class_sessions_type_start", columnNames = {"class_type_id", "start_at"})
        })
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Unique identifier for this session

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "class_type_id", nullable = false)
    private ClassType classType; // The type of class for this session

    @Column(name = "start_at", nullable = false)
    private Instant startAt; // Start time of the session (UTC)

    @Column(name = "end_at", nullable = false)
    private Instant endAt; // End time of the session (UTC)

    @Column(nullable = false)
    private int capacity; // Maximum number of allowed bookings

    @Column(nullable = false)
    private boolean canceled; // Whether the session has been canceled by admin

    @Column
    private String notes; // Optional notes or extra info for the session

    @Column(name = "created_by_admin_id", nullable = false)
    private Long createdByAdminId; // Audit: which admin created this session

    @Column(name = "created_at", nullable = false)
    private Instant createdAt; // Timestamp when the session was created\

}
