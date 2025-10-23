// src/main/java/com/gymsystem/payments/PaymentCallbackController.java
package com.gymsystem.payments;

import com.gymsystem.payments.dto.MockPaymentCallbackRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Fake provider webhook to mark payments as PAID/FAILED using a shared secret. */
@RestController
@RequestMapping("/api/v1/payments/callback")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final SubscriptionService service;

    @PostMapping("/mock")
    public ResponseEntity<Void> callback(
            @RequestHeader(name = "X-Payments-Secret", required = false) String secret,
            @Valid @RequestBody MockPaymentCallbackRequest payload
    ) {
        service.handleMockCallback(secret, payload);
        return ResponseEntity.noContent().build();
    }
}
