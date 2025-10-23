// src/main/java/com/gymsystem/checkin/CheckinCallbackController.java
package com.gymsystem.checkin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Provider callback endpoint secured by X-Provider-Secret header. */
@RestController
@RequestMapping("/api/v1/checkin/callback")
@RequiredArgsConstructor
public class CheckinCallbackController {

    private final CheckinService service;

    @PostMapping
    public ResponseEntity<Void> callback(
            @RequestHeader("X-Provider-Secret") String secret,
            @RequestBody CallbackPayload body
    ) {
        service.providerCallback(secret, body.getProviderRef(), body.isApproved());
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class CallbackPayload {
        @NotBlank private String providerRef; // value issued in start()
        private boolean approved;
    }
}
