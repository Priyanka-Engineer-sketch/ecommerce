package com.ecomm.saga.kafka.config;

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
public class UserKafkaTopicConfig {

    @Bean
    public KafkaAdmin userKafkaAdmin(KafkaProperties props) {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        return new KafkaAdmin(config);
    }

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("user.events.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoginTopic() {
        return TopicBuilder.name("user.events.login")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailOutboxTopic() {
        return TopicBuilder.name("user.events.email.outbox")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic passwordResetTopic() {
        return TopicBuilder.name("user.events.password.reset")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic fraudAlertTopic() {
        return TopicBuilder.name("user.events.fraud.alert")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
