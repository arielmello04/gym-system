// src/main/java/com/gymsystem/profile/ProfilePreferencesController.java
package com.gymsystem.profile;

import com.gymsystem.profile.dto.ProfilePreferencesResponse;
import com.gymsystem.profile.dto.UpdateProfilePreferencesRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/preferences")
@RequiredArgsConstructor
public class ProfilePreferencesController {

    private final ProfilePreferencesService service;

    @GetMapping
    public ResponseEntity<ProfilePreferencesResponse> getMy() {
        return ResponseEntity.ok(service.getMy());
    }

    @PutMapping
    public ResponseEntity<ProfilePreferencesResponse> updateMy(@Valid @RequestBody UpdateProfilePreferencesRequest req) {
        return ResponseEntity.ok(service.updateMy(req));
    }
}
