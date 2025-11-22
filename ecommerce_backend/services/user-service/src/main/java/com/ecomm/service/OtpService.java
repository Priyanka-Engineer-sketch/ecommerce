package com.ecomm.service;

import com.ecomm.entity.User;

public interface OtpService {
    void sendForgotPasswordOtp(String email);

    void verifyForgotPasswordOtp(String email, String otp);

    void sendFraudOtp(String email);

    void verifyFraudOtp(String email, String otp);

    void sendPasswordResetOtp(User user);

    boolean validateOtp(String email, String otp, String type);

    void sendOtp(String email, String purpose);
}
