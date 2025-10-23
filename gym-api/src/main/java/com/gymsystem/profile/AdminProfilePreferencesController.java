// src/main/java/com/gymsystem/profile/AdminProfilePreferencesController.java
package com.gymsystem.profile;

import com.gymsystem.profile.dto.ProfilePreferencesResponse;
import com.gymsystem.profile.dto.UpdateProfilePreferencesRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users/{userId}/preferences")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN_APP','ADMIN_WEB')")
public class AdminProfilePreferencesController {

    private final ProfilePreferencesService service;

    @GetMapping
    public ResponseEntity<ProfilePreferencesResponse> getForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getForUser(userId));
    }

    @PutMapping
    public ResponseEntity<ProfilePreferencesResponse> updateForUser(@PathVariable Long userId,
                                                                    @Valid @RequestBody UpdateProfilePreferencesRequest req) {
        return ResponseEntity.ok(service.updateForUser(userId, req));
    }
}
