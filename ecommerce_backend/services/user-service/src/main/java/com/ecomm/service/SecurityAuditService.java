package com.ecomm.service;

import com.ecomm.entity.User;

public interface SecurityAuditService {
    void log(User user, String action, String details, String ip, String userAgent);
}
