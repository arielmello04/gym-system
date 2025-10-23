// src/main/java/com/gymsystem/booking/ClassTypeRepository.java
package com.gymsystem.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for accessing class types.
 */
public interface ClassTypeRepository extends JpaRepository<ClassType, Long> {
    Optional<ClassType> findByCodeAndActiveTrue(String code); // Finds an active class type by its unique code
    Optional<ClassType> findByCode(String code);
}
