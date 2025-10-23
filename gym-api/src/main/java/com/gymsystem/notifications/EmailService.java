// src/main/java/com/gymsystem/notifications/EmailService.java
package com.gymsystem.notifications;

import com.gymsystem.user.User;

/** Simple email abstraction. Replace with SMTP/provider later. */
public interface EmailService {
    void sendPaymentReminder(User user, String subject, String body);
}
