package com.ecomm.saga.kafka.config;


import com.ecomm.saga.kafka.SagaKafkaTopics;
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
public class SagaKafkaTopicConfig {

    /**
     * This replaces deprecated buildAdminProperties()
     */
    @Bean
    public KafkaAdmin kafkaAdmin(KafkaProperties kafkaProperties) {
        Map<String, Object> configs = new HashMap<>();

        // REQUIRED → bootstrap servers
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers());

        // OPTIONAL → security (if SSL/SASL later)
        if (kafkaProperties.getProperties() != null) {
            configs.putAll(kafkaProperties.getProperties());
        }

        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic orderSagaStartTopic() {
        return TopicBuilder.name(SagaKafkaTopics.ORDER_SAGA_START)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderSagaCommandsInventoryTopic() {
        return TopicBuilder.name(SagaKafkaTopics.ORDER_SAGA_CMD_INVENTORY)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderSagaCommandsPaymentTopic() {
        return TopicBuilder.name(SagaKafkaTopics.ORDER_SAGA_CMD_PAYMENT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderSagaCommandsShippingTopic() {
        return TopicBuilder.name(SagaKafkaTopics.ORDER_SAGA_CMD_SHIPPING)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderSagaRepliesTopic() {
        return TopicBuilder.name(SagaKafkaTopics.ORDER_SAGA_REPLIES)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(SagaKafkaTopics.USER_REGISTERED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoginTopic() {
        return TopicBuilder.name(SagaKafkaTopics.USER_LOGIN)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userFraudAlertTopic() {
        return TopicBuilder.name(SagaKafkaTopics.USER_FRAUD_ALERT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userPasswordResetTopic() {
        return TopicBuilder.name(SagaKafkaTopics.USER_PASSWORD_RESET)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userEmailOutboxTopic() {
        return TopicBuilder.name(SagaKafkaTopics.USER_EMAIL_OUTBOX)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
