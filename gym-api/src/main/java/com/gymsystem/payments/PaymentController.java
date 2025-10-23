// src/main/java/com/gymsystem/payments/PaymentController.java
package com.gymsystem.payments;

import com.gymsystem.payments.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** User endpoints for subscriptions and invoices. */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionService service;

    @PostMapping("/subscribe")
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscribeRequest request) {
        return ResponseEntity.ok(service.subscribe(request));
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionResponse> mySubscription() {
        return ResponseEntity.ok(service.getMySubscription());
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<PaymentItem>> myInvoices() {
        return ResponseEntity.ok(service.listMyInvoices());
    }

    @DeleteMapping("/subscription")
    public ResponseEntity<Void> cancel() {
        service.cancelMySubscription();
        return ResponseEntity.noContent().build();
    }
}
