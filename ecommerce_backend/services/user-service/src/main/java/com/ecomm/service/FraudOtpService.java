package com.ecomm.service;

import com.ecomm.dto.response.AuthResponse;
import com.ecomm.entity.User;

public interface FraudOtpService {

    void createFraudOtpForLogin(User user, String ip, String userAgent);

    AuthResponse verifyFraudOtp(String email, String otpCode);

    void validateFraudOtp(String email, String otp);
}
