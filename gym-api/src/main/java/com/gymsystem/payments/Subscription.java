// src/main/java/com/gymsystem/payments/Subscription.java
package com.gymsystem.payments;

import com.gymsystem.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Subscription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "price_cents", nullable = false)
    private long priceCents;

    @Column(name = "currency", nullable = false)
    private String currency;

    /** Day-of-month to attempt renewal (we clamp months with fewer days). */
    @Column(name = "billing_day", nullable = false)
    private int billingDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "current_period_start", nullable = false)
    private Instant currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private Instant currentPeriodEnd;

    @Column(name = "next_billing_at", nullable = false)
    private Instant nextBillingAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "canceled_at")
    private Instant canceledAt;
}
