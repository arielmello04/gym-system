// src/main/java/com/gymsystem/auth/invite/AdminSignupTokenController.java
package com.gymsystem.auth.invite;

import com.gymsystem.auth.invite.dto.CreateTokenRequest;
import com.gymsystem.auth.invite.dto.SignupTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin endpoints to manage signup tokens. */
@RestController
@RequestMapping("/api/v1/admin/invite-tokens")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminSignupTokenController {

    private final SignupTokenService service;

    @PostMapping
    public ResponseEntity<SignupTokenResponse> create(@Valid @RequestBody CreateTokenRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @GetMapping
    public ResponseEntity<List<SignupTokenResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
