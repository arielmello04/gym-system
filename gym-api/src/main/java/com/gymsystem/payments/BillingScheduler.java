// src/main/java/com/gymsystem/payments/BillingScheduler.java
package com.gymsystem.payments;

import com.gymsystem.notifications.EmailService;
import com.gymsystem.payments.gateway.PaymentGateway;
import com.gymsystem.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.gymsystem.booking.BookingEnforcer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Periodically processes due invoices:
 * - tries to charge PENDING payments (max N attempts with backoff)
 * - on success: marks PAID, rolls subscription, creates next invoice
 * - on repeated failures: marks subscription PAST_DUE and sends reminder
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BillingScheduler {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentGateway gateway;
    private final EmailService emailService;
    private final BookingEnforcer bookingEnforcer;

    @Value("${payments.max-attempts:3}")
    private int maxAttempts;

    @Value("${payments.retry-backoff-minutes:60}")
    private long backoffMinutes;

    /** Runs every minute (dev). In prod, consider every 5-15 minutes. */
    @Scheduled(cron = "${payments.scheduler.cron:0 * * * * *}")
    public void run() {
        List<Payment> due = paymentRepository.findDuePending(Instant.now());
        for (Payment p : due) {
            // Honor backoff between attempts
            if (p.getLastAttemptAt() != null &&
                    Duration.between(p.getLastAttemptAt(), Instant.now()).toMinutes() < backoffMinutes) {
                continue;
            }
            tryCharge(p);
        }
    }

    @Transactional
    void tryCharge(Payment p) {
        // Refresh for transactional consistency
        p = paymentRepository.findById(p.getId()).orElse(null);
        if (p == null || p.getStatus() != PaymentStatus.PENDING) return;

        p.setAttemptCount(p.getAttemptCount() + 1);
        p.setLastAttemptAt(Instant.now());

        try {
            var res = gateway .charge(p);
            if (res.accepted()) {
                p.setStatus(PaymentStatus.PAID);
                p.setProviderRef(res.providerRef());
                p.setPaidAt(Instant.now());
                paymentRepository.save(p);

                rollSubscription(p.getSubscription()); // create next invoice
                return;
            }
        } catch (Exception e) {
            // log and fall-through to retry branch
            log.warn("Charge attempt failed paymentId={}", p.getId(), e);
        }

        // Failure path with retries
        if (p.getAttemptCount() >= maxAttempts) {
            paymentRepository.save(p);
            markPastDueAndNotify(p.getSubscription());
        } else {
            paymentRepository.save(p); // keep as PENDING for future retries
        }
    }

    private void markPastDueAndNotify(Subscription sub) {
        sub = subscriptionRepository.findById(sub.getId()).orElseThrow();
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            sub.setStatus(SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(sub);
        }

        int canceled = bookingEnforcer.enforcePastDue(sub.getUser().getId());
        log.info("PAST_DUE enforcement: userId={} canceledBookings={}", sub.getUser().getId(), canceled);

        User u = sub.getUser();
        emailService.sendPaymentReminder(
                u,
                "Payment failed - Action required",
                "Your subscription is past due and future class bookings were suspended. Please update your payment method."
        );
    }

    /** Rolls subscription period and creates next invoice when a charge succeeds. */
    @Transactional
    public void rollSubscription(Subscription s) {
        s = subscriptionRepository.findById(s.getId()).orElseThrow();

        // If subscriber was PAST_DUE and we got a successful charge, set back to ACTIVE
        if (s.getStatus() == SubscriptionStatus.PAST_DUE) {
            s.setStatus(SubscriptionStatus.ACTIVE);
        }

        // Move window forward one month based on the stored billingDay
        var nextPeriod = computeNextPeriodFromEnd(s, s.getCurrentPeriodEnd());
        s.setCurrentPeriodStart(nextPeriod.start());
        s.setCurrentPeriodEnd(nextPeriod.end());
        s.setNextBillingAt(nextPeriod.end());
        subscriptionRepository.save(s);

        // Create next invoice (PENDING) for the new cycle
        Payment next = Payment.builder()
                .subscription(s)
                .amountCents(s.getPriceCents())
                .currency(s.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider("MOCK")
                .dueAt(s.getNextBillingAt())
                .createdAt(Instant.now())
                .attemptCount(0)      // ensure Payment entity has these fields
                .build();
        paymentRepository.save(next);
    }

    // Minimal monthly step based on billingDay stored on subscription
    private record Period(java.time.Instant start, java.time.Instant end) {}
    private Period computeNextPeriodFromEnd(Subscription s, Instant prevEnd) {
        var z = prevEnd.atZone(java.time.ZoneOffset.UTC);
        var nextStart = clampDay(z, s.getBillingDay());
        var nextEnd = clampDay(z.plusMonths(1), s.getBillingDay());
        return new Period(nextStart.toInstant(), nextEnd.toInstant());
    }
    private java.time.ZonedDateTime clampDay(java.time.ZonedDateTime base, int billingDay) {
        int last = base.toLocalDate().lengthOfMonth();
        int day = Math.min(billingDay, last);
        return base.withDayOfMonth(day).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
}
