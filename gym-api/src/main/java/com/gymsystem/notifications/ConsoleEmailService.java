// src/main/java/com/gymsystem/notifications/ConsoleEmailService.java
package com.gymsystem.notifications;

import com.gymsystem.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Logs instead of sending real emails (dev only). */
@Component
@Slf4j
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendPaymentReminder(User user, String subject, String body) {
        log.info("[EMAIL][to={}] {} -- {}", user.getEmail(), subject, body);
    }
}
