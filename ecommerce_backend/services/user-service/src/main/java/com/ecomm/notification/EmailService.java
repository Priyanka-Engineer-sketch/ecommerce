package com.ecomm.notification;

import com.ecomm.entity.User;
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
import java.nio.file.Files;

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
    // 1. Welcome Email (After Register)
    // -------------------------------------------------
    public void sendWelcomeEmail(User user) {

        String template = loadTemplate("welcome.html");

        // If template empty -> fallback to inline HTML
        if (template.isBlank()) {
            template = """
                <h2>Welcome, %s!</h2>
                <p>Your account has been created successfully.</p>
            """.formatted(user.getUsername());
        }

        String html = template.replace("{{username}}", user.getUsername());

        sendHtmlEmail(user.getEmail(), "Welcome to E-Commerce!", html);
    }

    // -------------------------------------------------
    // 2. Email Verification Link
    // -------------------------------------------------
    public void sendVerificationEmail(User user, String token) {

        String template = loadTemplate("verify-email.html");

        String verifyUrl = frontendBaseUrl + "/verify-email?token=" + token;

        if (template.isBlank()) {
            template = """
                <h2>Email Verification</h2>
                <p>Hello %s,</p>
                <p>Click this link to verify your email:</p>
                <a href="%s">Verify Email</a>
            """.formatted(user.getUsername(), verifyUrl);
        }

        String html = template
                .replace("{{username}}", user.getUsername())
                .replace("{{verifyUrl}}", verifyUrl);

        sendHtmlEmail(user.getEmail(), "Verify your email address", html);
    }

    // -------------------------------------------------
    // 3. Login Notification: New Device / New IP
    // -------------------------------------------------
    public void sendLoginNotification(User user, String ip, String userAgent) {

        String template = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>New login to your account</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h2>New Login Detected</h2>\n" +
                "<p>Hi {{username}},</p>\n" +
                "<p>We noticed a new login to your account.</p>\n" +
                "<ul>\n" +
                "    <li><b>Email:</b> {{email}}</li>\n" +
                "    <li><b>IP Address:</b> {{ip}}</li>\n" +
                "    <li><b>Device:</b> {{userAgent}}</li>\n" +
                "</ul>\n" +
                "<p>If this was you, you can ignore this email.</p>\n" +
                "<p>If you don’t recognize this login, please change your password immediately.</p>\n" +
                "</body>\n" +
                "</html>\n";

        if (template.isBlank()) {
            template = """
                <h2>New Login Detected</h2>
                <p>Hello %s,</p>
                <p>A login to your account was detected.</p>
                <ul>
                   <li><b>IP:</b> %s</li>
                   <li><b>Device:</b> %s</li>
                </ul>
                <p>If this wasn't you, change your password immediately.</p>
            """.formatted(user.getUsername(), ip, userAgent);
        }

        String html = template
                .replace("{{username}}", user.getUsername())
                .replace("{{ip}}", ip)
                .replace("{{agent}}", userAgent);

        sendHtmlEmail(user.getEmail(), "New Login to Your Account", html);
    }

    public void sendPasswordResetOtp(User user, String otpCode) {
        String subject = "Password reset OTP";
        String body = """
            <h2>Password Reset</h2>
            <p>Hi %s,</p>
            <p>Your OTP code for password reset is:</p>
            <h3>%s</h3>
            <p>This code will expire in 10 minutes.</p>
            <p>If you did not request this, you can ignore this email.</p>
            """.formatted(user.getUsername(), otpCode);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

// ---------- Password changed notification ----------

    public void sendPasswordChangedEmail(User user) {
        String subject = "Your password has been changed";
        String body = """
            <h2>Password Changed</h2>
            <p>Hi %s,</p>
            <p>Your account password was changed recently.</p>
            <p>If this was not you, please contact support immediately.</p>
            """.formatted(user.getUsername());

        sendHtmlEmail(user.getEmail(), subject, body);
    }

// ---------- Profile updated notification ----------

    public void sendProfileUpdatedEmail(User user) {
        String subject = "Your profile was updated";
        String body = """
            <h2>Profile Updated</h2>
            <p>Hi %s,</p>
            <p>Your profile information has been updated.</p>
            <p>If you did not perform this action, please review your account security.</p>
            """.formatted(user.getUsername());

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ---------- Login 2FA OTP ----------
    public void sendLogin2faOtp(User user, String otpCode, String ip, String userAgent) {
        String subject = "Your login verification code";
        String body = """
            <h2>Login Verification</h2>
            <p>Hi %s,</p>
            <p>Your one-time code for login is:</p>
            <h3>%s</h3>
            <p>This code expires in 5 minutes.</p>
            <p>IP: %s</p>
            <p>Device: %s</p>
            """.formatted(user.getUsername(), otpCode, ip, userAgent);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ---------- Fraud alert email ----------
    public void sendFraudAlertEmail(User user, String ip, String userAgent, String level) {
        String subject = "Suspicious login activity detected";
        String body = """
            <h2>Suspicious Login Activity</h2>
            <p>Hi %s,</p>
            <p>We detected a login that looks <b>%s risk</b>.</p>
            <ul>
                <li>Email: %s</li>
                <li>IP: %s</li>
                <li>Device: %s</li>
            </ul>
            <p>If this was not you, please change your password immediately and review your account security.</p>
            """.formatted(user.getUsername(), level, user.getEmail(), ip, userAgent);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    // ---------- Fraud verification OTP ----------
    public void sendFraudVerificationOtp(User user, String otpCode, String ip, String userAgent) {
        String subject = "Suspicious login – verify it’s you";
        String body = """
            <h2>Verify suspicious login</h2>
            <p>Hi %s,</p>
            <p>We detected a high-risk login attempt on your account.</p>
            <p>Your verification code is:</p>
            <h3>%s</h3>
            <p>This code expires in 5 minutes.</p>
            <ul>
                <li>Email: %s</li>
                <li>IP Address: %s</li>
                <li>Device: %s</li>
            </ul>
            <p>If this wasn't you, we strongly recommend changing your password.</p>
            """.formatted(user.getUsername(), otpCode, user.getEmail(), ip, userAgent);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    public void sendForgotPasswordOtp(User user, String otp) {
        String subject = "Your Password Reset OTP";
        String body = """
        <h2>Password Reset Code</h2>
        <p>Hi %s,</p>
        <p>Your OTP code is:</p>
        <h3>%s</h3>
        <p>This code will expire in 10 minutes.</p>
        """.formatted(user.getUsername(), otp);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

    public void sendFraudAlertOtp(User user, String otp) {
        String subject = "⚠️ Verify Your Login – Security Check";
        String body = """
        <h2>Suspicious Login Detected</h2>
        <p>Hi %s,</p>
        <p>We detected a login attempt that looks unusual.</p>
        <p>To verify it's you, enter this OTP:</p>
        <h3>%s</h3>
        <p>This code expires in 15 minutes.</p>
        """.formatted(user.getUsername(), otp);

        sendHtmlEmail(user.getEmail(), subject, body);
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

    public void sendPasswordResetEmail(User user, String otp) {
        String subject = "Reset your E-Comm password";
        String body = """
                <h2>Password reset request</h2>
                <p>Hi %s,</p>
                <p>We received a request to reset the password for your account.</p>
                <p>Your one-time password (OTP) is:</p>
                <p style="font-size:20px;font-weight:bold;">%s</p>
                <p>This code is valid for 10 minutes.</p>
                <p>If you didn't request this, you can safely ignore this email.</p>
                """.formatted(user.getUsername(), otp);

        sendHtmlEmail(user.getEmail(), subject, body);
    }

}
