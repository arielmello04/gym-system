// src/main/java/com/gymsystem/payments/PaymentRepository.java
package com.gymsystem.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.Instant;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);
    // Payments due and still pending (for scheduler)
    @Query("select p from Payment p " +
            "where p.status = com.gymsystem.payments.PaymentStatus.PENDING " +
            "and p.dueAt <= :now " +
            "order by p.dueAt asc")
    List<Payment> findDuePending(Instant now);

    // Last created payment for a subscription (helps avoid duplicates)
    Payment findTopBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);

    // Count pending for a subscription (avoid generating too many)
    long countBySubscriptionIdAndStatus(Long subscriptionId, PaymentStatus status);
}

