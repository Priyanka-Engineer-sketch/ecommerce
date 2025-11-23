package com.ecomm.notification.kafka;

import com.ecomm.events.user.*;
import com.ecomm.notification.PushNotificationService;
import com.ecomm.notification.SmsService;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSecurityEventsConsumer {

    private final PushNotificationService pushService; // your own abstraction
    private final SmsService smsService;               // optional

    @KafkaListener(
            topics = SagaKafkaTopics.USER_LOGIN_SUSPICIOUS,
            groupId = "notification-security"
    )
    public void onSuspiciousLogin(@Payload UserLoginSuspiciousEvent event) {
        log.warn("Suspicious login event: {}", event);
        pushService.sendToUser(event.userId(), "Suspicious login detected on your account.");
        // optionally SMS
    }

    @KafkaListener(
            topics = SagaKafkaTopics.USER_LOGIN_FRAUD,
            groupId = "notification-security"
    )
    public void onFraudLogin(@Payload UserLoginFraudEvent event) {
        log.error("Fraud login event: {}", event);
        pushService.sendToUser(event.userId(), "We blocked a high-risk login attempt.");
        // escalate: SMS, email, call center, etc.
    }

    @KafkaListener(
            topics = SagaKafkaTopics.USER_PASSWORD_RESET,
            groupId = "notification-security"
    )
    public void onPasswordReset(@Payload UserPasswordResetEvent event) {
        log.info("Password reset event: {}", event);
        pushService.sendToUser(event.userId(), "Your password has been changed.");
    }

    @KafkaListener(
            topics = SagaKafkaTopics.USER_EMAIL_VERIFIED,
            groupId = "notification-security"
    )
    public void onEmailVerified(@Payload UserEmailVerifiedEvent event) {
        log.info("Email verified event: {}", event);
        pushService.sendToUser(event.userId(), "Your email was successfully verified.");
    }
}
