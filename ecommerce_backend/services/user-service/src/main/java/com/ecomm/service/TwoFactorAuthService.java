package com.ecomm.service;

import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.response.AuthResponse;

public interface TwoFactorAuthService {

    /**
     * Step 1: user submits email + password, we validate and send OTP to email.
     */
    void requestLoginOtp(LoginRequest req, String ip, String userAgent);

    /**
     * Step 2: user submits email + OTP, we verify and issue JWT tokens.
     */
    AuthResponse verifyLoginOtp(String email, String otpCode);
}

