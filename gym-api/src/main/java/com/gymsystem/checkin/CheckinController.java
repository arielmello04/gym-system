// src/main/java/com/gymsystem/checkin/CheckinController.java
package com.gymsystem.checkin;

import com.gymsystem.checkin.dto.StartCheckinRequest;
import com.gymsystem.checkin.dto.StartCheckinResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** User-facing check-in endpoints. */
@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CheckinController {

    private final CheckinService service;

    @PostMapping("/start")
    public ResponseEntity<StartCheckinResponse> start(@Valid @RequestBody StartCheckinRequest req) {
        return ResponseEntity.ok(service.start(req));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Checkin>> history() {
        return ResponseEntity.ok(service.myHistory());
    }
}
