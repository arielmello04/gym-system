// src/main/java/com/gymsystem/checkin/CheckinService.java
package com.gymsystem.checkin;

import com.gymsystem.checkin.dto.StartCheckinRequest;
import com.gymsystem.checkin.dto.StartCheckinResponse;
import com.gymsystem.user.User;
import com.gymsystem.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/** Encapsulates provider-specific check-in flows. */
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRepository repository;
    private final UserRepository userRepository;

    @Value("${checkin.callback-secret}")
    private String callbackSecret;

    /** Starts a check-in depending on the provider. DIRECT completes instantly. */
    @Transactional
    public StartCheckinResponse start(StartCheckinRequest req) {
        User user = currentUser();

        CheckinProvider provider = CheckinProvider.valueOf(req.getProvider().toUpperCase());
        String providerRef = "CHK-" + UUID.randomUUID();

        Checkin c = Checkin.builder()
                .user(user)
                .provider(provider)
                .gymName(provider == CheckinProvider.DIRECT ? req.getGymName() : null)
                .providerRef(providerRef)
                .status(CheckinStatus.STARTED)
                .startedAt(Instant.now())
                .build();
        var saved = repository.save(c);

        // For Gympass/TotalPass we return a mock deep link; real flow will redirect to provider app
        if (provider == CheckinProvider.GYMPASS || provider == CheckinProvider.TOTALPASS) {
            // The app would open this URL; provider would callback our endpoint later.
            String redirect = "gymsystem://checkin/provider?ref=" + providerRef + "&provider=" + provider.name();
            return new StartCheckinResponse(redirect, saved.getId());
        }

        // DIRECT payers: complete immediately (no external app)
        c.setStatus(CheckinStatus.COMPLETED);
        c.setCompletedAt(Instant.now());
        repository.save(c);
        return new StartCheckinResponse(null, saved.getId());
    }

    /** Called by provider after user authorizes check-in in their app. */
    @Transactional
    public void providerCallback(String providedSecret, String providerRef, boolean approved) {
        if (!callbackSecret.equals(providedSecret)) {
            throw new SecurityException("Invalid provider secret");
        }
        var c = repository.findByProviderRef(providerRef)
                .orElseThrow(() -> new IllegalArgumentException("Unknown providerRef"));

        if (approved) {
            c.setStatus(CheckinStatus.COMPLETED);
            c.setCompletedAt(Instant.now());
        } else {
            c.setStatus(CheckinStatus.FAILED);
        }
        repository.save(c);
    }

    /** Current user's check-in history. */
    public java.util.List<Checkin> myHistory() {
        return repository.findByUserIdOrderByStartedAtDesc(currentUser().getId());
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}
