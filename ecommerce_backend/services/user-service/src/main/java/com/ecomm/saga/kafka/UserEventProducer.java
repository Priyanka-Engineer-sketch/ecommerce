package com.ecomm.saga.kafka;

import com.ecomm.entity.User;
import com.ecomm.events.user.UserEmailOutboxEvent;
import com.ecomm.events.user.UserFraudAlertEvent;
import com.ecomm.events.user.UserLoginEvent;
import com.ecomm.events.user.UserPasswordResetEvent;
import com.ecomm.events.user.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                System.currentTimeMillis()
        );
        kafkaTemplate.send(SagaKafkaTopics.USER_REGISTERED,
                user.getId().toString(), event);
    }

    public void sendLoginEvent(User user, String ip, String userAgent, int riskScore) {
        UserLoginEvent event = new UserLoginEvent(
                user.getId(),
                user.getEmail(),
                ip,
                userAgent,
                riskScore,
                System.currentTimeMillis()
        );
        kafkaTemplate.send(SagaKafkaTopics.USER_LOGIN,
                user.getId().toString(), event);
    }

    public void sendFraudAlert(User user, int riskScore, String reason) {
        UserFraudAlertEvent event = new UserFraudAlertEvent(
                user.getId(),
                user.getEmail(),
                riskScore,
                reason,
                System.currentTimeMillis()
        );
        kafkaTemplate.send(SagaKafkaTopics.USER_FRAUD_ALERT,
                user.getId().toString(), event);
    }

    public void sendPasswordReset(User user) {
        UserPasswordResetEvent event = new UserPasswordResetEvent(
                user.getId(),
                user.getEmail(),
                System.currentTimeMillis()
        );
        kafkaTemplate.send(SagaKafkaTopics.USER_PASSWORD_RESET,
                user.getId().toString(), event);
    }

    public void sendEmailOutbox(String to, String template, String payloadJson) {
        UserEmailOutboxEvent event = new UserEmailOutboxEvent(
                to,
                template,
                payloadJson,
                System.currentTimeMillis()
        );
        kafkaTemplate.send(SagaKafkaTopics.USER_EMAIL_OUTBOX, to, event);
    }
}
