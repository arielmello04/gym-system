// src/main/java/com/gymsystem/booking/ClassSessionRepository.java
package com.gymsystem.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

/**
 * Repository for accessing class sessions.
 */
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    /**
     * Finds sessions that are not canceled within the given time window (inclusive).
     * Results are ordered by start time ascending.
     */
    @Query(""" 
        SELECT s
        FROM ClassSession s
        WHERE s.canceled = false
          AND s.startAt >= :from
          AND s.endAt   <= :to
        ORDER BY s.startAt ASC
    """)
    List<ClassSession> findActiveSessionsBetween(@Param("from") Instant from, @Param("to") Instant to);

    @Query("""
   select s from ClassSession s
     join fetch s.classType t
   where s.startAt between :from and :to
     and (:typeCode is null or t.code = :typeCode)
   order by s.startAt asc
   """)
    List<ClassSession> findCalendar(@Param("from") Instant from, @Param("to") Instant to, @Param("typeCode") String typeCode);

}
