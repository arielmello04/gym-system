// src/main/java/com/gymsystem/profile/ProfilePreferencesService.java
package com.gymsystem.profile;

import com.gymsystem.profile.dto.ProfilePreferencesResponse;
import com.gymsystem.profile.dto.UpdateProfilePreferencesRequest;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProfilePreferencesService {

    private final ProfilePreferencesRepository repository;
    private final UserRepository userRepository;

    public ProfilePreferencesResponse getMy() {
        User user = currentUser();
        var pref = repository.findByUserId(user.getId()).orElseGet(() -> defaultFor(user));
        return toResponse(pref);
    }

    @Transactional
    public ProfilePreferencesResponse updateMy(UpdateProfilePreferencesRequest req) {
        User user = currentUser();
        var pref = repository.findByUserId(user.getId()).orElseGet(() -> defaultFor(user));

        if (req.getAllowRecording() != null) pref.setAllowRecording(req.getAllowRecording());
        if (req.getAllowPhotos() != null) pref.setAllowPhotos(req.getAllowPhotos());
        if (req.getAllowFaceVisibility() != null) pref.setAllowFaceVisibility(req.getAllowFaceVisibility());
        if (req.getNotes() != null) pref.setNotes(req.getNotes());

        pref.setUpdatedAt(Instant.now());
        repository.save(pref);
        return toResponse(pref);
    }

    // Admin read/update for any user
    public ProfilePreferencesResponse getForUser(Long userId) {
        var userPref = repository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Preferences not found for user"));
        return toResponse(userPref);
    }

    @Transactional
    public ProfilePreferencesResponse updateForUser(Long userId, UpdateProfilePreferencesRequest req) {
        var pref = repository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Preferences not found for user"));

        if (req.getAllowRecording() != null) pref.setAllowRecording(req.getAllowRecording());
        if (req.getAllowPhotos() != null) pref.setAllowPhotos(req.getAllowPhotos());
        if (req.getAllowFaceVisibility() != null) pref.setAllowFaceVisibility(req.getAllowFaceVisibility());
        if (req.getNotes() != null) pref.setNotes(req.getNotes());

        pref.setUpdatedAt(Instant.now());
        repository.save(pref);
        return toResponse(pref);
    }

    private ProfilePreferences defaultFor(User user) {
        return repository.save(ProfilePreferences.builder()
                .user(user)
                .allowRecording(true)
                .allowPhotos(true)
                .allowFaceVisibility(true)
                .notes(null)
                .updatedAt(Instant.now())
                .build());
    }

    private ProfilePreferencesResponse toResponse(ProfilePreferences p) {
        return new ProfilePreferencesResponse(
                p.isAllowRecording(),
                p.isAllowPhotos(),
                p.isAllowFaceVisibility(),
                p.getNotes(),
                p.getUpdatedAt()
        );
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
