// src/main/java/com/gymsystem/booking/BookingPolicyRepository.java
package com.gymsystem.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for accessing the single booking policy row.
 */
public interface BookingPolicyRepository extends JpaRepository<BookingPolicy, Long> {
    Optional<BookingPolicy> findTopByOrderByIdAsc(); // Retrieves the first policy row (singleton pattern)
}
