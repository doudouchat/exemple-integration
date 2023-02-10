package com.exemple.integration.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import jakarta.annotation.PostConstruct;

@Configuration
public class InitKafka {

    @Value("${authorization.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Bean
    public KafkaConsumer<String, Map<String, Object>> consumerNewPassword() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress,
                ConsumerConfig.GROUP_ID_CONFIG, "test",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new KafkaConsumer<>(props);
    }

    @PostConstruct
    public void suscribeConsumerNewPassword() throws Exception {

        consumerNewPassword().subscribe(List.of("new_password"), new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                consumerNewPassword().seekToBeginning(partitions);
            }
        });
    }

}
