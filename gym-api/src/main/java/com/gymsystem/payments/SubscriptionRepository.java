// src/main/java/com/gymsystem/payments/SubscriptionRepository.java
package com.gymsystem.payments;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserIdAndStatusIn(Long userId, Iterable<SubscriptionStatus> statuses);
}
