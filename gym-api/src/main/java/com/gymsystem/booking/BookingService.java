// src/main/java/com/gymsystem/booking/BookingService.java
package com.gymsystem.booking;

import com.gymsystem.booking.dto.AdminCreateSessionRequest;
import com.gymsystem.booking.dto.AvailabilityItem;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.gymsystem.payments.SubscriptionRepository;
import com.gymsystem.payments.SubscriptionStatus;
import com.gymsystem.i18n.I18n;
import com.gymsystem.common.ratelimit.RateLimiter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import com.gymsystem.booking.config.BookingConfigService;
import java.time.*;

/**
 * Application service encapsulating booking business rules.
 * Note: this version enforces the global BookingPolicy (openDaysInAdvance).
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ClassTypeRepository classTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingPolicyRepository policyRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BookingConfigService bookingConfigService;
    private final I18n i18n;
    private final RateLimiter rateLimiter;

    @Value("${ratelimit.bookings.book-min-interval-ms:800}")
    private long bookMinIntervalMs;

    @Value("${ratelimit.bookings.cancel-min-interval-ms:800}")
    private long cancelMinIntervalMs;

    /**
     * Creates a class session on behalf of an admin.
     * @param request the session creation parameters
     * @return the id of the newly created session
     */
    @Transactional
    public Long createSession(AdminCreateSessionRequest request) {
        var admin = getCurrentUser(); // Resolves the current authenticated admin user
        var classType = classTypeRepository.findByCodeAndActiveTrue(request.getClassTypeCode()) // Looks up active class type
                .orElseThrow(() -> new IllegalArgumentException("Unknown or inactive class type code: " + request.getClassTypeCode())); // Throws if not found

        // Validate times and capacity
        if (!request.getStartAt().isBefore(request.getEndAt())) { // Ensures start is strictly before end
            throw new IllegalArgumentException("startAt must be before endAt"); // Throws when invalid
        }
        if (request.getCapacity() <= 0) { // Ensures capacity is positive
            throw new IllegalArgumentException("capacity must be > 0"); // Throws when invalid
        }

        var now = Instant.now();
        if (request.getEndAt().isBefore(now)) { // Prevents creating sessions fully in the past
            throw new IllegalArgumentException("Cannot create sessions in the past"); // Throws when invalid
        }

        var session = ClassSession.builder() // Builds a new session entity
                .classType(classType)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt()) // Sets end time
                .capacity(request.getCapacity())
                .canceled(false) // Initializes as not canceled
                .notes(request.getNotes()) // Optional notes
                .createdByAdminId(admin.getId()) // Audits creator admin id
                .createdAt(now) // Creation timestamp
                .build(); // Finishes building

        var saved = classSessionRepository.save(session); // Persists the session
        return saved.getId();
    }

    /**
     * Cancels (soft-deletes) a session by id if there are no active bookings.
     * @param sessionId the id of the session to cancel
     */
    @Transactional
    public void cancelSession(Long sessionId) {
        getCurrentUser(); // Ensures the caller is authenticated (admin enforced at controller)
        var session = classSessionRepository.findById(sessionId) // Loads session
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId)); // Throws if missing

        long active = bookingRepository.countActiveBySessionId(sessionId); // Counts active bookings
        if (active > 0) { // If there are active bookings
            throw new IllegalStateException("Cannot cancel a session with active bookings"); // Refuse cancellation
        }

        session.setCanceled(true); // Soft-cancels the session
        classSessionRepository.save(session); // Persists change
    }

    /**
     * Returns availability between two instants but ALSO applies the current policy window.
     * This ensures the client only sees sessions they are allowed to book.
     * @param from inclusive lower bound requested by client
     * @param to inclusive upper bound requested by client
     * @return a list of availability items respecting the booking policy window
     */
    public List<AvailabilityItem> getAvailability(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to must be provided");
        }
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }

        var now = Instant.now();
        var policy = policyRepository.findTopByOrderByIdAsc().orElse(null);
        Instant policyUpper = (policy == null)
                ? Instant.MAX
                : now.plus(policy.getOpenDaysInAdvance(), ChronoUnit.DAYS);

        Instant effectiveTo = to.isBefore(policyUpper) ? to : policyUpper;

        if (!from.isBefore(effectiveTo)) {
            return List.of();
        }

        var sessions = classSessionRepository.findActiveSessionsBetween(from, effectiveTo);

        var cfg = bookingConfigService.get();
        var nowZ = ZonedDateTime.now(ZoneOffset.UTC);

        List<AvailabilityItem> items = new ArrayList<>();
        for (var s : sessions) {

            var z = s.getStartAt().atZone(ZoneOffset.UTC);
            var firstDay = z.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            var openAt = firstDay.minusDays(cfg.getPublishDaysBeforeMonth());

            if (nowZ.isBefore(openAt)) {
                continue;
            }

            long booked = bookingRepository.countActiveBySessionId(s.getId());
            long spotsLeft = Math.max(0, s.getCapacity() - booked);

            items.add(new AvailabilityItem(
                    s.getId(),
                    s.getClassType().getCode(),
                    s.getClassType().getName(),
                    s.getStartAt(),
                    s.getEndAt(),
                    s.getCapacity(),
                    spotsLeft,
                    s.getNotes()
            ));
        }
        return items;
    }

    /**
     * Books a spot in a given session for the current user, enforcing the policy window.
     * @param sessionId the id of the session to book
     * @return a response DTO representing the new booking
     */
    @Transactional
    public com.gymsystem.booking.dto.BookingResponse bookSession(Long sessionId) {
        // Resolve current authenticated user (throws if missing)
        var user = getCurrentUser();
        // Prevent accidental double-click bursts for the same user
        rateLimiter.enforceMinInterval(
                "book:" + user.getId(),
                bookMinIntervalMs,
                "Too many booking attempts; please wait a moment"
        );

        // Business gate: only ACTIVE subscribers can book
        assertUserHasActiveSubscription(user.getId());

        // Load target session or fail fast
        var session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        var now = Instant.now();

        // Reject bookings for canceled sessions
        if (session.isCanceled()) {
            throw new IllegalStateException(i18n.msg("booking.session.canceled"));
        }

        // Booking must happen strictly before session start
        if (!now.isBefore(session.getStartAt())) {
            throw new IllegalStateException(i18n.msg("booking.already.started"));
        }

        // ---- Booking window enforcement (intersection of two rules) ----
        // Rule A: global “open days in advance” (e.g., 15 days ahead)
        var policy = policyRepository.findTopByOrderByIdAsc().orElse(null);
        if (policy != null) {
            Instant horizon = now.plus(policy.getOpenDaysInAdvance(), ChronoUnit.DAYS);
            if (session.getStartAt().isAfter(horizon)) {
                throw new IllegalStateException(i18n.msg("booking.horizon.exceeded"));
            }
        }

        // Rule B: monthly publish window (“open next month X days before the 1st”)
        // Example: for a November session, booking is allowed only when now >= (Nov-01 - publishDaysBeforeMonth)
        assertBookingWindowAllows(session.getStartAt());

        var cfg = bookingConfigService.get();
        if (cfg.isOnePerDayPerType()) {
            var z = session.getStartAt().atZone(ZoneOffset.UTC);
            var dayStart = z.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
            var dayEnd   = z.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

            long already =
                bookingRepository.countActiveForUserByTypeAndDay(
                    user.getId(), session.getClassType().getId(), dayStart, dayEnd
                );
            if (already > 0) {
                throw new IllegalStateException(i18n.msg("booking.oneperday"));
            }
        }

        // Prevent duplicate active booking for the same session and user
        var already = bookingRepository.findActiveBySessionIdAndUserId(sessionId, user.getId());
        if (already.isPresent()) {
            throw new IllegalStateException(i18n.msg("booking.duplicate.session"));
        }

        // Enforce capacity
        long activeCount = bookingRepository.countActiveBySessionId(sessionId);
        if (activeCount >= session.getCapacity()) {
            throw new IllegalStateException(i18n.msg("booking.full"));
        }

        // Persist booking
        var booking = Booking.builder()
                .session(session)
                .user(user)
                .status(BookingStatus.BOOKED)
                .createdAt(now)
                .build();

        var saved = bookingRepository.save(booking);

        return new com.gymsystem.booking.dto.BookingResponse(
                saved.getId(),
                sessionId,
                saved.getStatus().name(),
                saved.getCreatedAt(),
                saved.getCanceledAt()
        );
    }


    /**
     * Cancels a booking owned by the current user (idempotent).
     * @param bookingId the id of the booking to cancel
     */
    @Transactional // Ensures atomic update
    public void cancelMyBooking(Long bookingId) {
        var user = getCurrentUser(); // Resolves current user

        // Prevent rapid duplicate cancels (UI hammering)
        rateLimiter.enforceMinInterval(
                "cancel:" + user.getId(),
                cancelMinIntervalMs,
                "Too many cancel attempts; please wait a moment"
        );
        
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId()) // Loads booking owned by user
                .orElseThrow(() -> new IllegalArgumentException("Booking not found for the current user: " + bookingId)); // Throws if not found
        
        if (booking.getStatus() == BookingStatus.CANCELED) { // If already canceled
            return; // Idempotent: do nothing
        }   

        var cfg = bookingConfigService.get();
        int cutoff = Math.max(0, cfg.getCancelCutoffHours()); // guard against negative values
        Instant sessionStart = booking.getSession().getStartAt();
        Instant latestAllowed = sessionStart.minusSeconds(cutoff * 3600L);

        // If now is after or equal the latest allowed cancellation instant, block the cancellation.
        if (!Instant.now().isBefore(latestAllowed)) {
            throw new IllegalStateException(i18n.msg("booking.cancel.cutoff"));
        }
        
        var now = Instant.now(); // Current time
        booking.setStatus(BookingStatus.CANCELED); // Updates status
        booking.setCanceledAt(now); // Sets cancellation time
        bookingRepository.save(booking); // Persists change
    }

    /**
     * Resolves the current authenticated user from the security context.
     * @return the User entity for the current principal
     */
    private User getCurrentUser() {
        var email = SecurityContextHolder.getContext().getAuthentication().getName(); // Reads principal email
        return userRepository.findByEmail(email) // Loads user by email
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email)); // Throws if missing
    }

    private void assertUserHasActiveSubscription(Long userId) {
        var sub = subscriptionRepository.findByUserIdAndStatusIn(userId, Set.of(SubscriptionStatus.ACTIVE));
        if (sub.isEmpty()) {
            throw new IllegalStateException("User does not have an active subscription");
        }
    }
    /** Blocks booking if the session's month isn't open yet based on publishDaysBeforeMonth. */
    private void assertBookingWindowAllows(Instant sessionStart) {
        var cfg = bookingConfigService.get();
        var zSession = sessionStart.atZone(ZoneOffset.UTC);
        var firstDayOfMonth = zSession.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        var openAt = firstDayOfMonth.minusDays(cfg.getPublishDaysBeforeMonth());
        if (ZonedDateTime.now(ZoneOffset.UTC).isBefore(openAt)) {
            // i18n here too
            throw new IllegalStateException(i18n.msg("booking.month.not.open"));
        }
    }
}
