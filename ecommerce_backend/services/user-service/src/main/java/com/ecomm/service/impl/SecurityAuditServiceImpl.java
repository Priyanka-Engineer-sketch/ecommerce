package com.ecomm.service.impl;

import com.ecomm.entity.User;
import com.ecomm.entity.domain.SecurityAuditLog;
import com.ecomm.repository.SecurityAuditLogRepository;
import com.ecomm.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SecurityAuditLogRepository repo;

    @Override
    public void log(User user, String action, String details, String ip, String userAgent) {
        SecurityAuditLog log = SecurityAuditLog.builder()
                .userId(user != null ? user.getId() : null)
                .email(user != null ? user.getEmail() : null)
                .action(action)
                .details(details)
                .ip(ip)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build();
        repo.save(log);
    }
}
