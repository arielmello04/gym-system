// src/main/java/com/gymsystem/payments/AdminBillingController.java
package com.gymsystem.payments;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/billing")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminBillingController {

    private final BillingScheduler scheduler;

    @PostMapping("/run-once")
    public ResponseEntity<Void> runOnce() {
        scheduler.run();
        return ResponseEntity.accepted().build();
    }
}
