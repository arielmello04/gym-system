// src/main/java/com/gymsystem/booking/BookingRepository.java
package com.gymsystem.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List; 
import java.time.Instant;
import java.util.Optional;

/**
 * Repository for accessing bookings.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Counts how many active (BOOKED) bookings exist for the given session.
     * This helps enforce capacity limits.
     */
    @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE b.session.id = :sessionId AND b.status = 'BOOKED'
    """)
    long countActiveBySessionId(@Param("sessionId") Long sessionId);

    /**
     * Finds an active booking for a user in a given session (if any).
     * This prevents duplicate bookings by the same user.
     */
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.session.id = :sessionId AND b.user.id = :userId AND b.status = 'BOOKED'
    """)
    Optional<Booking> findActiveBySessionIdAndUserId(@Param("sessionId") Long sessionId, @Param("userId") Long userId);

    /**
     * Finds a booking by id that belongs to a specific user (of any status).
     * Used for safe cancellation by the booking owner.
     */
    @Query("""
        SELECT b
        FROM Booking b
        WHERE b.id = :bookingId AND b.user.id = :userId
    """)
    Optional<Booking> findByIdAndUserId(@Param("bookingId") Long bookingId, @Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Booking b
        set b.status = com.gymsystem.booking.BookingStatus.CANCELED,
            b.canceledAt = :now
        where b.user.id = :userId
        and b.status = com.gymsystem.booking.BookingStatus.BOOKED
        and b.session.startAt > :cutoff
    """)
    int cancelFutureActiveByUser(@Param("userId") Long userId,
                                @Param("cutoff") Instant cutoff,
                                @Param("now") Instant now);

    /**
     * Fetches all bookings for a user with session and class type eagerly loaded,
     * sorted by session start date (newest first for convenience).
     */
    @Query("""
        select b
          from Booking b
          join fetch b.session s
          join fetch s.classType t
         where b.user.id = :userId
         order by s.startAt desc
    """)
    List<Booking> findAllByUserIdWithSession(@Param("userId") Long userId);
    
    /**
     * Counts active (BOOKED) bookings for a user, for sessions of the same class type,
     * whose startAt lies in [dayStart, dayEnd).
     * Used to enforce "one booking per day per class type".
     */
    @Query("""
        select count(b)
          from Booking b
          join b.session s
          join s.classType t
         where b.user.id = :userId
           and b.status = com.gymsystem.booking.BookingStatus.BOOKED
           and t.id = :classTypeId
           and s.startAt >= :dayStart and s.startAt < :dayEnd
    """)
    long countActiveForUserByTypeAndDay(@Param("userId") Long userId,
                                        @Param("classTypeId") Long classTypeId,
                                        @Param("dayStart") Instant dayStart,
                                        @Param("dayEnd") Instant dayEnd);
}
