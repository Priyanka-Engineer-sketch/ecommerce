package com.ecomm.saga.kafka.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SagaKafkaProducerConfig {

    @Bean
    public ProducerFactory<String, Object> sagaProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();

        // 1) Global bootstrap servers from spring.kafka.bootstrap-servers
        List<String> bootstrapServers = kafkaProperties.getBootstrapServers();
        if (bootstrapServers != null && !bootstrapServers.isEmpty()) {
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, String.join(",", bootstrapServers));
        }

        // 2) Copy producer-specific properties from spring.kafka.producer.*
        KafkaProperties.Producer producerProps = kafkaProperties.getProducer();

        // acks, retries, etc. (only set if configured)
        if (producerProps.getAcks() != null) {
            props.put(ProducerConfig.ACKS_CONFIG, producerProps.getAcks());
        }
        if (producerProps.getRetries() != null) {
            props.put(ProducerConfig.RETRIES_CONFIG, producerProps.getRetries());
        }

        // serializers
        if (producerProps.getKeySerializer() != null) {
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerProps.getKeySerializer());
        }
        if (producerProps.getValueSerializer() != null) {
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerProps.getValueSerializer());
        }

        // custom spring.kafka.producer.properties.*
        if (producerProps.getProperties() != null) {
            props.putAll(producerProps.getProperties());
        }

        // 3) Also merge generic spring.kafka.properties.* (if any)
        if (kafkaProperties.getProperties() != null) {
            props.putAll(kafkaProperties.getProperties());
        }

        // 4) Enforce saga-wide idempotent producer behaviour
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        // keep this high but finite; your YAML already has retries: 3
        props.putIfAbsent(ProducerConfig.RETRIES_CONFIG, 3);
        // safe with idempotence (ordering)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> sagaKafkaTemplate(ProducerFactory<String, Object> sagaProducerFactory) {
        return new KafkaTemplate<>(sagaProducerFactory);
    }
}
