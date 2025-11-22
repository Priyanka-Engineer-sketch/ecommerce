package com.ecomm.service;

public interface ForgotPasswordService {
    void sendPasswordResetOtp(String email);
    void resetPassword(String otp, String newPassword);
}
