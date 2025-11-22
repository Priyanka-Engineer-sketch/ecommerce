package com.ecomm.controller;

import com.ecomm.dto.response.OtpAuditResponse;
import com.ecomm.entity.domain.otp.FraudOtpToken;
import com.ecomm.entity.domain.otp.PasswordResetToken;
import com.ecomm.repository.FraudOtpTokenRepository;
import com.ecomm.repository.OtpTokenRepository;
import com.ecomm.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/security")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SecurityAdminController {

    private final OtpTokenRepository otpRepo;
    private final PasswordResetTokenRepository prRepo;
    private final FraudOtpTokenRepository fraudOtpRepo;

    @GetMapping("/otp")
    public List<OtpAuditResponse> listOtp(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return otpRepo.findAll(pageable)
                .map(t -> OtpAuditResponse.builder()
                        .id(t.getId())
                        .email(t.getEmail())
                        .type(t.getType())
                        .consumed(t.isUsed())
                        .createdAt(t.getCreatedAt())
                        .expiresAt(t.getExpiresAt())
                        .build())
                .getContent();
    }

    @GetMapping("/password-reset-otp")
    public List<OtpAuditResponse> listPasswordResetOtp(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return prRepo.findAll(pageable)
                .map(t -> OtpAuditResponse.builder()
                        .id(t.getId())
                        .email(t.getEmail())
                        .type("PASSWORD_RESET")
                        .consumed(t.isConsumed())
                        .createdAt(t.getCreatedAt())
                        .expiresAt(t.getExpiresAt())
                        .build())
                .getContent();
    }

    @GetMapping("/fraud-otp")
    public List<OtpAuditResponse> listFraudOtp(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return fraudOtpRepo.findAll(pageable)
                .map(t -> OtpAuditResponse.builder()
                        .id(t.getId())
                        .email(t.getEmail())
                        .type("FRAUD_VERIFY")
                        .consumed(t.isConsumed())
                        .createdAt(t.getCreatedAt())
                        .expiresAt(t.getExpiresAt())
                        .build())
                .getContent();
    }
}
