package com.ecomm.reco.kafka;

import com.ecomm.events.user.UserLoginSuccessEvent;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.reco.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoginEventsConsumer {

    private final UserActivityService userActivityService;

    @KafkaListener(
            topics = SagaKafkaTopics.USER_LOGIN_SUCCESS,
            groupId = "recommender-logins"
    )
    public void onLoginSuccess(@Payload UserLoginSuccessEvent event) {
        log.info("Recommender received USER_LOGIN_SUCCESS: {}", event);
        userActivityService.registerLogin(
                event.userId(),
                event.ip(),
                event.userAgent(),
                event.timestamp()
        );
    }
}
