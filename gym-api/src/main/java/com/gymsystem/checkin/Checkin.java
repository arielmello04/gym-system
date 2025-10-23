// src/main/java/com/gymsystem/checkin/Checkin.java
package com.gymsystem.checkin;

import com.gymsystem.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/** Persists a single check-in operation for auditing and reporting. */
@Entity
@Table(name = "checkins")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Checkin {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckinProvider provider;

    /** Optional: for DIRECT payer (non-gympass/totalpass). */
    @Column(name = "gym_name", length = 120)
    private String gymName;

    /** Provider-specific reference (or mock) to correlate callbacks. */
    @Column(name = "provider_ref", length = 120)
    private String providerRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckinStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
