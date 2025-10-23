// src/main/java/com/gymsystem/payments/gateway/MockPaymentGateway.java
package com.gymsystem.payments.gateway;

import com.gymsystem.payments.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Mock gateway: auto-accept or auto-fail based on a toggle (for demos). */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Value("${payments.mock.accept:true}")
    private boolean accept; // set to false to simulate failures

    @Override
    public ChargeResult charge(Payment payment) {
        // Pretend we called an external API and got a reference back
        String ref = "MOCK-" + payment.getId() + "-" + Instant.now().toEpochMilli();
        return new ChargeResult(accept, ref);
    }
}
