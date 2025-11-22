package com.ecomm.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEmailListeners {

    private final EmailService emailService;

    @Value("${app.mail.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    // ---------------- PASSWORD CHANGED ----------------
    @Async
    @EventListener
    public void onPasswordChanged(SecurityEvents.PasswordChangedEvent e) {
        try {
            String body = """
                    <h2>Password Changed</h2>
                    <p>Hi,</p>
                    <p>Your password was successfully changed.</p>
                    <p>If this wasn't you, please reset your password immediately or contact support.</p>
                    """;

            emailService.sendHtmlEmail(e.email(), "Your password was changed", body);
            log.info("Sent password-changed email to {}", e.email());
        } catch (Exception ex) {
            log.error("Failed to send password-changed email to {}: {}", e.email(), ex.getMessage());
        }
    }

    // ---------------- ACCOUNT ACTIVATED / DEACTIVATED ----------------
    @Async
    @EventListener
    public void onStatusChanged(SecurityEvents.AccountStatusChangedEvent e) {
        try {
            boolean active = e.active();

            String subject = active ? "Account Activated" : "Account Deactivated";
            String body = active
                    ? """
                        <h2>Account Activated</h2>
                        <p>Your account has been activated and is now fully usable.</p>
                      """
                    : """
                        <h2>Account Deactivated</h2>
                        <p>Your account has been deactivated.</p>
                        <p>If you believe this is a mistake, contact support immediately.</p>
                      """;

            emailService.sendHtmlEmail(e.email(), subject, body);
            log.info("Sent account-status email ({}) to {}", subject, e.email());
        } catch (Exception ex) {
            log.error("Failed to send account-status email to {}: {}", e.email(), ex.getMessage());
        }
    }

    // ---------------- EMAIL VERIFICATION ----------------
    @Async
    @EventListener
    public void onEmailVerification(SecurityEvents.EmailVerificationRequestedEvent e) {
        try {
            String verifyUrl = frontendBaseUrl + "/verify-email?token=" + e.token();

            String body = """
                    <h2>Email Verification</h2>
                    <p>Please verify your email address by clicking the link below:</p>
                    <p><a href="%s">Verify Email</a></p>
                    <p>This link is valid for 24 hours.</p>
                    """.formatted(verifyUrl);

            emailService.sendHtmlEmail(e.email(), "Verify Your Email", body);
            log.info("Sent email-verification email to {}", e.email());
        } catch (Exception ex) {
            log.error("Failed to send email-verification email to {}: {}", e.email(), ex.getMessage());
        }
    }
}
