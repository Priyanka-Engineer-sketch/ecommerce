package com.ecomm.saga.kafka.config;

import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@ConditionalOnMissingBean(name = "kafkaAdmin")
public class KafkaTopicConfig {

    private final KafkaProperties kafkaProperties;

    // -----------------------------------------------------
    // KAFKA ADMIN (Single Bean)
    // -----------------------------------------------------
    @Bean
    @ConditionalOnMissingBean(KafkaAdmin.class)
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers());

        if (kafkaProperties.getProperties() != null) {
            configs.putAll(kafkaProperties.getProperties());
        }

        return new KafkaAdmin(configs);
    }

    // Helper to create topics
    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // -----------------------------------------------------
    // ORDER SAGA TOPICS
    // -----------------------------------------------------
    @Bean public NewTopic orderSagaStart()            { return topic(SagaKafkaTopics.ORDER_SAGA_START); }
    @Bean public NewTopic orderSagaInvCmd()           { return topic(SagaKafkaTopics.ORDER_SAGA_CMD_INVENTORY); }
    @Bean public NewTopic orderSagaPayCmd()           { return topic(SagaKafkaTopics.ORDER_SAGA_CMD_PAYMENT); }
    @Bean public NewTopic orderSagaShipCmd()          { return topic(SagaKafkaTopics.ORDER_SAGA_CMD_SHIPPING); }
    @Bean public NewTopic orderSagaReplies()          { return topic(SagaKafkaTopics.ORDER_SAGA_REPLIES); }

    // -----------------------------------------------------
    // USER EVENTS (Unified both sets)
    // -----------------------------------------------------
    @Bean public NewTopic userRegistered()            { return topic(SagaKafkaTopics.USER_REGISTERED); }
    @Bean public NewTopic userRegisteredAdmin()       { return topic(SagaKafkaTopics.USER_REGISTERED_BY_ADMIN); }

    @Bean public NewTopic userLoginSuccess()          { return topic(SagaKafkaTopics.USER_LOGIN_SUCCESS); }
    @Bean public NewTopic userLoginSuspicious()       { return topic(SagaKafkaTopics.USER_LOGIN_SUSPICIOUS); }
    @Bean public NewTopic userLoginFraud()            { return topic(SagaKafkaTopics.USER_LOGIN_FRAUD); }

    @Bean public NewTopic userRefreshToken()          { return topic(SagaKafkaTopics.USER_REFRESH_TOKEN); }
    @Bean public NewTopic userLogoutAll()             { return topic(SagaKafkaTopics.USER_LOGOUT_ALL); }

    @Bean public NewTopic userPasswordReset()         { return topic(SagaKafkaTopics.USER_PASSWORD_RESET); }
    @Bean public NewTopic userEmailVerified()         { return topic(SagaKafkaTopics.USER_EMAIL_VERIFIED); }

    @Bean public NewTopic userEmailOutbox()           { return topic(SagaKafkaTopics.USER_EMAIL_OUTBOX); }

    // Fraud alert from UserKafkaTopicConfig
    @Bean public NewTopic userFraudAlertTopic()       { return topic(SagaKafkaTopics.USER_FRAUD_ALERT); }

    // Additional topics from UserKafkaTopicConfig
    @Bean public NewTopic userEventsRegistered()      { return topic("user.events.registered"); }
    @Bean public NewTopic userEventsLogin()           { return topic("user.events.login"); }
    @Bean public NewTopic userEventsPasswordReset()   { return topic("user.events.password.reset"); }
    @Bean public NewTopic userEventsFraudAlert()      { return topic("user.events.fraud.alert"); }
    @Bean public NewTopic userEventsEmailOutbox()     { return topic("user.events.email.outbox"); }

}
