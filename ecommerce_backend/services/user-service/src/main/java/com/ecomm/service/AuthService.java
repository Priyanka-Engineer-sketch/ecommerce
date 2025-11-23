package com.ecomm.service;

import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.UserResponse;

public interface AuthService {
    UserResponse registerUser(RegisterRequest req);

    AuthResponse register(RegisterRequest req);

    AuthResponse login(LoginRequest req, String ip, String userAgent);

    AuthResponse refresh(String refreshToken);

    AuthResponse issueTokensForUser(String email);

    void logoutAll(Long userId);
}
