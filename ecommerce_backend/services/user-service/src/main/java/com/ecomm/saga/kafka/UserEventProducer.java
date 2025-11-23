package com.ecomm.saga.kafka;

import com.ecomm.entity.User;
import com.ecomm.events.user.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafka;

    private String eventId() {
        return UUID.randomUUID().toString();
    }

    // ============================
    // USER REGISTERED
    // ============================
    public void sendUserRegisteredEvent(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                eventId(), user.getId(), user.getEmail(), user.getUsername(),
                System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_REGISTERED, user.getId().toString(), event);
        log.info("EVENT → USER_REGISTERED {}", event);
    }

    public void sendUserRegisteredByAdminEvent(User user) {
        UserRegisteredByAdminEvent event = new UserRegisteredByAdminEvent(
                eventId(), user.getId(), user.getEmail(), user.getUsername(),
                System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_REGISTERED_BY_ADMIN, user.getId().toString(), event);
        log.info("EVENT → USER_REGISTERED_BY_ADMIN {}", event);
    }

    // ============================
    // LOGIN EVENTS
    // ============================
    public void sendUserLoginSuccessEvent(User user, String ip, String agent) {
        UserLoginSuccessEvent event = new UserLoginSuccessEvent(
                eventId(), user.getId(), user.getEmail(), ip, agent, System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_LOGIN_SUCCESS, user.getId().toString(), event);
    }

    public void sendUserSuspiciousLoginEvent(User user, String ip, String agent, int score) {
        UserLoginSuspiciousEvent event = new UserLoginSuspiciousEvent(
                eventId(), user.getId(), user.getEmail(), ip, agent, score, System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_LOGIN_SUSPICIOUS, user.getId().toString(), event);
    }

    public void sendUserFraudLoginEvent(User user, String ip, String agent) {
        UserLoginFraudEvent event = new UserLoginFraudEvent(
                eventId(), user.getId(), user.getEmail(), ip, agent, System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_LOGIN_FRAUD, user.getId().toString(), event);
    }

    // ============================
    // SECURITY EVENTS
    // ============================
    public void sendPasswordReset(User user) {
        UserPasswordResetEvent event = new UserPasswordResetEvent(
                eventId(), user.getId(), user.getEmail(), System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_PASSWORD_RESET, user.getId().toString(), event);
    }

    public void sendUserEmailVerifiedEvent(User user) {
        UserEmailVerifiedEvent event = new UserEmailVerifiedEvent(
                eventId(), user.getId(), user.getEmail(), System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_EMAIL_VERIFIED, user.getId().toString(), event);
    }

    public void sendUserRefreshTokenEvent(User user) {
        UserRefreshTokenEvent event = new UserRefreshTokenEvent(
                eventId(), user.getId(), user.getEmail(), System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_REFRESH_TOKEN, user.getId().toString(), event);
    }

    public void sendLogoutAllEvent(Long userId) {
        UserLogoutAllEvent event = new UserLogoutAllEvent(
                eventId(), userId, System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_LOGOUT_ALL, userId.toString(), event);
    }

    // ============================
    // EMAIL OUTBOX
    // ============================
    public void sendEmailOutbox(String to, String template, String payload) {
        UserEmailOutboxEvent event = new UserEmailOutboxEvent(
                eventId(), to, template, payload, System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_EMAIL_OUTBOX, to, event);
    }

    // ✅ NEW: generic fraud alert with score + reason (for your existing usage)
    public void sendFraudAlert(User user, int riskScore, String reason) {
        UserFraudAlertEvent event = new UserFraudAlertEvent(
                eventId(),
                user.getId(),
                user.getEmail(),
                riskScore,
                reason,
                System.currentTimeMillis()
        );
        kafka.send(SagaKafkaTopics.USER_FRAUD_ALERT, user.getId().toString(), event);
        log.info("EVENT → USER_FRAUD_ALERT {}", event);
    }
}
