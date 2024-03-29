package com.exemple.integration.core;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fasterxml.jackson.databind.JsonNode;

@Configuration
public class InitKafka {

    @Value("${authorization.kafka.bootstrap-servers}")
    private String authorizationBootstrapAddress;

    @Value("${event.kafka.bootstrap-servers}")
    private String serviceBootstrapAddress;

    @Bean
    public KafkaConsumer<String, Map<String, Object>> consumerNewPassword() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, authorizationBootstrapAddress,
                ConsumerConfig.GROUP_ID_CONFIG, "password_test",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    @Bean
    public KafkaConsumer<String, JsonNode> consumerEvent() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, serviceBootstrapAddress,
                ConsumerConfig.GROUP_ID_CONFIG, "service_test",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.VALUE_DEFAULT_TYPE, JsonNode.class,
                JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new KafkaConsumer<>(props);
    }

}
