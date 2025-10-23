// src/main/java/com/gymsystem/payments/SubscriptionService.java
package com.gymsystem.payments;

import com.gymsystem.payments.dto.*;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.gymsystem.booking.BookingEnforcer;
import com.gymsystem.notifications.EmailService;
import java.time.*;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BookingEnforcer bookingEnforcer;
    private final EmailService emailService;

    @Value("${payments.callback-secret:change-me-too}")
    private String callbackSecret;

    /** Creates a monthly subscription anchored on the user's createdAt day-of-month. */
    @Transactional
    public SubscriptionResponse subscribe(SubscribeRequest req) {
        User user = getCurrentUser();

        // prevent duplicate active subscription
        var existing = subscriptionRepository.findByUserIdAndStatusIn(
                user.getId(), Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE));
        if (existing.isPresent()) {
            throw new IllegalStateException("User already has an active subscription");
        }

        Instant now = Instant.now();
        int billingDay = getSafeBillingDay(user.getCreatedAt());

        // Compute current period (start now, end at next occurrence of billingDay)
        var period = computeMonthlyPeriod(now, billingDay);
        Subscription sub = Subscription.builder()
                .user(user)
                .planName(req.getPlanName())
                .priceCents(req.getPriceCents())
                .currency(req.getCurrency())
                .billingDay(billingDay)
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodStart(period.start())
                .currentPeriodEnd(period.end())
                .nextBillingAt(period.end()) // charge at end boundary
                .createdAt(now)
                .build();
        Subscription saved = subscriptionRepository.save(sub);

        // Create initial invoice (PENDING)
        Payment p = Payment.builder()
                .subscription(saved)
                .amountCents(req.getPriceCents())
                .currency(req.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider("MOCK")
                .dueAt(period.end())
                .createdAt(now)
                .build();
        paymentRepository.save(p);

        return toResponse(saved);
    }

    /** Returns the current user's active/past-due subscription, if any. */
    public SubscriptionResponse getMySubscription() {
        User user = getCurrentUser();
        var sub = subscriptionRepository.findByUserIdAndStatusIn(
                        user.getId(), Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE))
                .orElseThrow(() -> new IllegalStateException("Subscription not found"));
        return toResponse(sub);
    }

    /** Lists invoices of the current user's subscription. */
    public List<PaymentItem> listMyInvoices() {
        User user = getCurrentUser();
        var sub = subscriptionRepository.findByUserIdAndStatusIn(
                        user.getId(), Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE))
                .orElseThrow(() -> new IllegalStateException("Subscription not found"));
        return paymentRepository.findBySubscriptionIdOrderByCreatedAtDesc(sub.getId())
                .stream()
                .map(this::toPaymentItem)
                .toList();
    }

    /** Cancels subscription at period end (soft-cancel); prevents new invoices. */
    @Transactional
    public void cancelMySubscription() {
        User user = getCurrentUser();
        var sub = subscriptionRepository.findByUserIdAndStatusIn(
                        user.getId(), Set.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE))
                .orElseThrow(() -> new IllegalStateException("Subscription not found"));
        sub.setStatus(SubscriptionStatus.CANCELED);
        sub.setCanceledAt(Instant.now());
        subscriptionRepository.save(sub);
    }

    /** Mock provider callback to mark a payment as PAID/FAILED. */
    @Transactional
    public void handleMockCallback(String providedSecret, MockPaymentCallbackRequest payload) {
        if (providedSecret == null || !providedSecret.equals(callbackSecret)) {
            throw new SecurityException("Invalid payments callback secret");
        }
        Payment p = paymentRepository.findById(payload.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (p.getStatus() != PaymentStatus.PENDING) return;

        if (Boolean.TRUE.equals(payload.getApproved())) {
            p.setStatus(PaymentStatus.PAID);
            p.setPaidAt(Instant.now());
            paymentRepository.save(p);

            // If PAST_DUE, rollSubscription sett ACTIVE
            rollSubscription(p.getSubscription());
            var sub = subscriptionRepository.findById(p.getSubscription().getId()).orElseThrow();
            if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
                emailService.sendPaymentReminder(
                        sub.getUser(),
                        "Payment received",
                        "Thanks! Your subscription is active again. You can book new classes now."
                );
            }
        } else {
            p.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(p);

            Subscription s = p.getSubscription();
            s.setStatus(SubscriptionStatus.PAST_DUE);
            subscriptionRepository.save(s);

            int canceled = bookingEnforcer.enforcePastDue(s.getUser().getId());
            emailService.sendPaymentReminder(
                    s.getUser(),
                    "Payment failed - Action required",
                    "Your subscription is past due and future class bookings were suspended. Please update your payment method."
            );
        }
    }

    // --- Helpers ---

    private record Period(Instant start, Instant end) {}

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getId(), s.getPlanName(), s.getPriceCents(), s.getCurrency(),
                s.getBillingDay(), s.getStatus().name(),
                s.getCurrentPeriodStart(), s.getCurrentPeriodEnd(), s.getNextBillingAt()
        );
    }

    private PaymentItem toPaymentItem(Payment p) {
        return new PaymentItem(
                p.getId(), p.getAmountCents(), p.getCurrency(),
                p.getStatus().name(), p.getProviderRef(),
                p.getDueAt(), p.getPaidAt(), p.getCreatedAt()
        );
    }

    /** Day-of-month based on user.createdAt (1..28/29/30/31) */
    private int getSafeBillingDay(Instant createdAt) {
        ZonedDateTime z = createdAt.atZone(ZoneOffset.UTC);
        return z.getDayOfMonth();
    }

    /** Computes [start, end] of the current monthly period using a fixed billing day. */
    private Period computeMonthlyPeriod(Instant anchor, int billingDay) {
        ZonedDateTime now = anchor.atZone(ZoneOffset.UTC);
        ZonedDateTime start;
        ZonedDateTime end;

        // If today is before the billing day in this month, period started last month on billingDay.
        if (now.getDayOfMonth() < billingDay) {
            start = clampDay(now.minusMonths(1), billingDay);
            end   = clampDay(now, billingDay);
        } else {
            start = clampDay(now, billingDay);
            end   = clampDay(now.plusMonths(1), billingDay);
        }
        return new Period(start.toInstant(), end.toInstant());
    }

    /** Adjust day-of-month if the month has fewer days (e.g., billingDay=31 on February). */
    private ZonedDateTime clampDay(ZonedDateTime base, int billingDay) {
        int lastDay = base.toLocalDate().lengthOfMonth();
        int day = Math.min(billingDay, lastDay);
        return base.withDayOfMonth(day).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }

    /**
     * Rolls the subscription to the next monthly period after a successful payment
     * and creates the next pending invoice. If the subscription is canceled, it does nothing.
     */
    private void rollSubscription(Subscription sub) {
        // Do nothing if the subscription was canceled meanwhile
        if (sub.getStatus() == SubscriptionStatus.CANCELED) {
            return;
        }

        // Next period starts exactly at the current period end boundary
        int billingDay = sub.getBillingDay();
        var nextPeriod = computeMonthlyPeriod(sub.getCurrentPeriodEnd(), billingDay);

        // Move the subscription window forward one cycle
        sub.setCurrentPeriodStart(nextPeriod.start());
        sub.setCurrentPeriodEnd(nextPeriod.end());
        sub.setNextBillingAt(nextPeriod.end());
        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(sub);

        // Create the next invoice as PENDING (will be charged at the new period end)
        Payment nextInvoice = Payment.builder()
                .subscription(sub)
                .amountCents(sub.getPriceCents())
                .currency(sub.getCurrency())
                .status(PaymentStatus.PENDING)
                .provider("MOCK")
                .dueAt(nextPeriod.end())
                .createdAt(Instant.now())
                .build();

        paymentRepository.save(nextInvoice);
    }

}

