package com.ecomm.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityFlags {
    @Value("${app.security.require-email-verified:true}")
    public boolean requireEmailVerified;

    @Value("${app.security.auto-verify-on-register:false}")
    public boolean autoVerifyOnRegister;
}
