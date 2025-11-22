package com.ecomm.service;

import com.ecomm.dto.response.AuthResponse;
import com.ecomm.entity.User;

public interface FraudOtpService {

    void createFraudOtpForLogin(User user, String ip, String userAgent);

    void validateFraudOtp(String email, String otp);
}
