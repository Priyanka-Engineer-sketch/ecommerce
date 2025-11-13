package com.ecomm.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityEmailListeners {
    private final EmailService email;

    @Async @EventListener
    public void onPasswordChanged(SecurityEvents.PasswordChangedEvent e) {
        email.send(e.email(), "Your password was changed",
                "Hi,\n\nYour password was changed. If this wasn't you, contact support immediately.");
    }

    @Async @EventListener
    public void onStatusChanged(SecurityEvents.AccountStatusChangedEvent e) {
        email.send(e.email(), e.active() ? "Account activated" : "Account deactivated",
                e.active()
                        ? "Hi,\n\nYour account has been activated."
                        : "Hi,\n\nYour account has been deactivated. If this wasn't you, contact support.");
    }

    @Async @EventListener
    public void onEmailVerification(SecurityEvents.EmailVerificationRequestedEvent e) {
        String link = "https://your-frontend/verify-email?token=" + e.token(); // TODO change to your FE URL
        email.send(e.email(), "Verify your email",
                "Hi,\n\nPlease verify your email by clicking the link below (valid 24h):\n" + link);
    }
}
