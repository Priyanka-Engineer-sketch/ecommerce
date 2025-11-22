package com.ecomm.controller;

import com.ecomm.dto.request.*;
import com.ecomm.service.OtpService;
import com.ecomm.service.AuthService;
import com.ecomm.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    // ----------- FORGOT PASSWORD FLOW -----------

    @PostMapping("/forgot-password")
    public void forgotPassword(@RequestBody ForgotPasswordRequest req) {
        otpService.sendForgotPasswordOtp(req.email());
    }

    @PostMapping("/forgot-password/verify")
    public void verifyForgotPassword(@RequestBody FraudOtpVerifyRequest req) {
        otpService.verifyForgotPasswordOtp(req.email(), req.otp());
    }

    @PostMapping("/forgot-password/reset")
    public void resetPassword(@RequestBody ResetPasswordRequest req) {
        otpService.verifyForgotPasswordOtp(req.email(), req.otp());
        passwordResetService.resetPassword(req.email(),req.otp(), req.newPassword());
    }

    // ----------- FRAUD CHECK OTP -----------

    @PostMapping("/fraud/send-otp")
    public void sendFraudOtp(@RequestBody FraudOtpRequest req) {
        otpService.sendFraudOtp(req.email());
    }

    @PostMapping("/fraud/verify")
    public void verifyFraudOtp(@RequestBody FraudOtpVerifyRequest req) {
        otpService.verifyFraudOtp(req.email(), req.otp());
    }
}
