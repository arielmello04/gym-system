// src/main/java/com/gymsystem/payments/AdminReportsService.java
package com.gymsystem.payments;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Minimal admin reporting. Use proper analytics/BI later. */
@Service
@RequiredArgsConstructor
public class AdminReportsService {

    private final EntityManager em;

    /** Sum of PAID amounts grouped by yyyy-MM (UTC). */
    public Map<String, Long> revenueByMonth(Instant from, Instant to) {
        List<Tuple> rows = em.createQuery(
                        "select FUNCTION('to_char', p.paidAt, 'YYYY-MM') as ym, sum(p.amountCents) as total " +
                                "from Payment p " +
                                "where p.status = com.gymsystem.payments.PaymentStatus.PAID " +
                                "and p.paidAt between :from and :to " +
                                "group by FUNCTION('to_char', p.paidAt, 'YYYY-MM') " +
                                "order by ym", Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return rows.stream().collect(Collectors.toMap(
                t -> t.get("ym", String.class),
                t -> ((Number) t.get("total")).longValue(),
                (a,b) -> a,
                java.util.LinkedHashMap::new
        ));
    }

    public Map<String, Long> churnByMonth(Instant from, Instant to) {
        List<Tuple> rows = em.createQuery(
                        "select FUNCTION('to_char', s.canceledAt, 'YYYY-MM') as ym, count(s.id) as cnt " +
                                "from Subscription s " +
                                "where s.status = com.gymsystem.payments.SubscriptionStatus.CANCELED " +
                                "and s.canceledAt between :from and :to " +
                                "group by FUNCTION('to_char', s.canceledAt, 'YYYY-MM') " +
                                "order by ym", Tuple.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        return rows.stream().collect(Collectors.toMap(
                t -> t.get("ym", String.class),
                t -> ((Number) t.get("cnt")).longValue(),
                (a,b) -> a,
                java.util.LinkedHashMap::new
        ));
    }
}
