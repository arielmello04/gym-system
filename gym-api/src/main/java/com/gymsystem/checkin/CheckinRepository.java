// src/main/java/com/gymsystem/checkin/CheckinRepository.java
package com.gymsystem.checkin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {
    List<Checkin> findByUserIdOrderByStartedAtDesc(Long userId);
    Optional<Checkin> findByProviderRef(String providerRef);
}
