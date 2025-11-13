package com.ecomm.notification;

public class SecurityEvents {
    public record PasswordChangedEvent(Long userId, String email) {}
    public record AccountStatusChangedEvent(Long userId, String email, boolean active) {}
    public record EmailVerificationRequestedEvent(Long userId, String email, String token) {}
}
