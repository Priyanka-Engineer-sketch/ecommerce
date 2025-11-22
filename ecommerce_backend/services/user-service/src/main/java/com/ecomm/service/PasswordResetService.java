package com.ecomm.service;

public interface PasswordResetService {

    void requestPasswordReset(String email);

    void resetPassword(String email, String otpCode, String newPassword);
}
