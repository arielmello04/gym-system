// src/main/java/com/gymsystem/payments/gateway/PaymentGateway.java
package com.gymsystem.payments.gateway;

import com.gymsystem.payments.Payment;

/** Abstraction for external providers (Stripe/Mercado Pago/etc.). */
public interface PaymentGateway {

    /** Attempts to charge the given payment. Returns provider reference if created. */
    ChargeResult charge(Payment payment) throws Exception;

    record ChargeResult(boolean accepted, String providerRef) {}
}
