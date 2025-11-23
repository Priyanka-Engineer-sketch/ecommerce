package com.ecomm.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${MAIL_FROM:noreply@yourdomain.com}")
    private String defaultFrom;

    @Value("${MAIL_TEMPLATE_PREFIX:mail/templates/}")
    private String templatePrefix;

    @Value("${app.mail.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    // -------------------------------------------------
    // Load HTML template file (used for welcome/verify/alerts)
    // -------------------------------------------------
    private String loadTemplate(String filename) {
        String path = templatePrefix + filename;
        try {
            ClassPathResource resource = new ClassPathResource(path);
            try (var is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Could not load email template from classpath: {}", path, e);
            return ""; // fallback to empty template so caller uses inline HTML
        }
    }

    // -------------------------------------------------
    // Generic HTML Email Sender
    // -------------------------------------------------
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");

            helper.setFrom(defaultFrom);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(msg);

            log.info("Email sent successfully to {}", to);

        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    // -------------------------------------------------
    // Below are extra helpers (mostly used in user-service).
    // You can keep them or delete if not needed in order-service.
    // -------------------------------------------------

    public void sendWelcomeEmail(User user) {
        String template = loadTemplate("welcome.html");

        if (template.isBlank()) {
            template = """
                <h2>Welcome, %s!</h2>
                <p>Your account has been created successfully.</p>
            """.formatted(user.userName());
        }

        String html = template.replace("{{username}}", user.userName());
        sendHtmlEmail(user.email(), "Welcome to E-Commerce!", html);
    }

    public void sendVerificationEmail(User user, String token) {
        String template = loadTemplate("verify-email.html");

        String verifyUrl = frontendBaseUrl + "/verify-email?token=" + token;

        if (template.isBlank()) {
            template = """
                <h2>Email Verification</h2>
                <p>Hello %s,</p>
                <p>Click this link to verify your email:</p>
                <a href="%s">Verify Email</a>
            """.formatted(user.userName(), verifyUrl);
        }

        String html = template
                .replace("{{username}}", user.userName())
                .replace("{{verifyUrl}}", verifyUrl);

        sendHtmlEmail(user.email(), "Verify your email address", html);
    }

    public void sendOtpEmail(String email, String otp, int minutes) {
        String subject = "Your OTP Code";
        String body = """
        <h2>Your OTP Code</h2>
        <p>Use this OTP to complete your request:</p>
        <h3>%s</h3>
        <p>This code is valid for %d minutes.</p>
        <br>
        <p>If you did not request this, ignore this email.</p>
        """.formatted(otp, minutes);

        sendHtmlEmail(email, subject, body);
    }

    // ... you can add/remove more methods as needed
}
