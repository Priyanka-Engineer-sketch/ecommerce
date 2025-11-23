package com.ecomm.email.kafka;

import com.ecomm.email.EmailSender;
import com.ecomm.events.user.UserEmailOutboxEvent;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEmailOutboxConsumer {

    private final EmailSender emailSender;

    @KafkaListener(
            topics = SagaKafkaTopics.USER_EMAIL_OUTBOX,
            groupId = "email-service-outbox"
    )
    public void onEmailOutbox(@Payload UserEmailOutboxEvent event) {
        log.info("Received USER_EMAIL_OUTBOX: {}", event);

        try {
            emailSender.sendTemplateEmail(
                    event.to(),
                    event.template(),
                    event.payload()
            );
        } catch (Exception ex) {
            log.error("Failed to send email for outbox event {}: {}", event.eventId(), ex.getMessage(), ex);
            // TODO: optionally publish a DLQ / failure topic
        }
    }
}
