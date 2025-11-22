package com.ecomm.controller;

import com.ecomm.entity.User;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/emails")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EmailController {

    private final EmailService emailService;
    private final UserRepository userRepository;

    // 1) Simple test email to any address
    @PostMapping("/test")
    public ResponseEntity<Void> sendTest(@RequestBody TestEmailRequest req) {
        emailService.sendHtmlEmail(req.getTo(), req.getSubject(), req.getBody());
        return ResponseEntity.ok().build();
    }

    // 2) Resend welcome email to user by id
    @PostMapping("/welcome/{userId}")
    public ResponseEntity<Void> resendWelcome(@PathVariable Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        emailService.sendWelcomeEmail(u);
        return ResponseEntity.ok().build();
    }

    // 3) Resend login notification manually (rare) by id
    @PostMapping("/login-notification/{userId}")
    public ResponseEntity<Void> resendLoginNotification(@PathVariable Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        emailService.sendLoginNotification(u, "127.0.0.1", "Manual trigger");
        return ResponseEntity.ok().build();
    }

    @Data
    public static class TestEmailRequest {
        @Email
        private String to;
        @NotBlank
        private String subject;
        @NotBlank
        private String body;
    }
}
