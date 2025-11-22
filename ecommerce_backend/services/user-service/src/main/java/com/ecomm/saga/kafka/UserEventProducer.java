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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private String newEventId() {
        return UUID.randomUUID().toString();
    }

    public void sendUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                newEventId(),                     // <- add this field in event class
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                System.currentTimeMillis()
        );
        log.info("Publishing USER_REGISTERED: {}", event);
        kafkaTemplate.send(
                SagaKafkaTopics.USER_REGISTERED,
                user.getId().toString(),
                event
        );
    }

    public void sendLoginEvent(User user, String ip, String userAgent, int riskScore) {
        UserLoginEvent event = new UserLoginEvent(
                newEventId(),
                user.getId(),
                user.getEmail(),
                ip,
                userAgent,
                riskScore,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );
        log.info("Publishing USER_LOGIN: {}", event);
        kafkaTemplate.send(
                SagaKafkaTopics.USER_LOGIN,
                user.getId().toString(),
                event
        );
    }

    public void sendFraudAlert(User user, int riskScore, String reason) {
        UserFraudAlertEvent event = new UserFraudAlertEvent(
                newEventId(),
                user.getId(),
                user.getEmail(),
                riskScore,
                reason,
                System.currentTimeMillis()
        );
        log.info("Publishing USER_FRAUD_ALERT: {}", event);
        kafkaTemplate.send(
                SagaKafkaTopics.USER_FRAUD_ALERT,
                user.getId().toString(),
                event
        );
    }

    public void sendPasswordReset(User user) {
        UserPasswordResetEvent event = new UserPasswordResetEvent(
                newEventId(),
                user.getId(),
                user.getEmail(),
                System.currentTimeMillis()
        );
        log.info("Publishing USER_PASSWORD_RESET: {}", event);
        kafkaTemplate.send(
                SagaKafkaTopics.USER_PASSWORD_RESET,
                user.getId().toString(),
                event
        );
    }

    public void sendEmailOutbox(String to, String template, String payloadJson) {
        UserEmailOutboxEvent event = new UserEmailOutboxEvent(
                newEventId(),
                to,
                template,
                payloadJson,
                System.currentTimeMillis()
        );
        log.info("Publishing USER_EMAIL_OUTBOX: {}", event);
        kafkaTemplate.send(
                SagaKafkaTopics.USER_EMAIL_OUTBOX,
                to,
                event
        );
    }
}
