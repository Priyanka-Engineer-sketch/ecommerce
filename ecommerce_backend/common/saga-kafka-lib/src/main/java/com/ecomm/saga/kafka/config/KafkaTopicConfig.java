package com.ecomm.saga.kafka.config;

import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(config);
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ======================================
    // ORDER SAGA TOPICS
    // ======================================
    @Bean public NewTopic orderSagaStart()        { return topic(SagaKafkaTopics.ORDER_SAGA_START); }
    @Bean public NewTopic orderSagaInvCmd()       { return topic(SagaKafkaTopics.ORDER_SAGA_CMD_INVENTORY); }
    @Bean public NewTopic orderSagaPayCmd()       { return topic(SagaKafkaTopics.ORDER_SAGA_CMD_PAYMENT); }
    @Bean public NewTopic orderSagaShipCmd()      { return topic(SagaKafkaTopics.ORDER_SAGA_CMD_SHIPPING); }
    @Bean public NewTopic orderSagaReplies()      { return topic(SagaKafkaTopics.ORDER_SAGA_REPLIES); }

    // ======================================
    // USER TOPICS
    // ======================================
    @Bean public NewTopic userRegistered()        { return topic(SagaKafkaTopics.USER_REGISTERED); }
    @Bean public NewTopic userRegisteredAdmin()   { return topic(SagaKafkaTopics.USER_REGISTERED_BY_ADMIN); }

    @Bean public NewTopic userLoginSuccess()      { return topic(SagaKafkaTopics.USER_LOGIN_SUCCESS); }
    @Bean public NewTopic userLoginSuspicious()   { return topic(SagaKafkaTopics.USER_LOGIN_SUSPICIOUS); }
    @Bean public NewTopic userLoginFraud()        { return topic(SagaKafkaTopics.USER_LOGIN_FRAUD); }

    @Bean public NewTopic userPasswordReset()     { return topic(SagaKafkaTopics.USER_PASSWORD_RESET); }
    @Bean public NewTopic userEmailVerified()     { return topic(SagaKafkaTopics.USER_EMAIL_VERIFIED); }

    @Bean public NewTopic userRefreshToken()      { return topic(SagaKafkaTopics.USER_REFRESH_TOKEN); }
    @Bean public NewTopic userLogoutAll()         { return topic(SagaKafkaTopics.USER_LOGOUT_ALL); }

    @Bean public NewTopic userEmailOutbox()       { return topic(SagaKafkaTopics.USER_EMAIL_OUTBOX); }
}
