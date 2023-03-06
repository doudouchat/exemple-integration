package com.exemple.integration.core;

import java.util.Collection;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.authorization.core.client.resource.AuthorizationClientResourceConfiguration;
import com.exemple.service.application.core.ApplicationConfiguration;
import com.exemple.service.resource.core.ResourceConfiguration;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import jakarta.annotation.PostConstruct;

@Configuration
@Import({ ResourceConfiguration.class, ApplicationConfiguration.class, AuthorizationClientResourceConfiguration.class })
@ComponentScan(basePackages = "com.exemple.integration")
public class IntegrationTestConfiguration {

    public static final String AUTHORIZATION_URL = System.getProperty("authorization.host", "http://localhost") + ":"
            + System.getProperty("authorization.port", "8090") + "/" + System.getProperty("authorization.contextpath", "ExempleAuthorization");

    @Value("${event.topic}")
    private String topic;

    @Autowired
    private KafkaConsumer<?, ?> consumerEvent;

    @Autowired
    private KafkaConsumer<?, ?> consumerNewPassword;

    @Bean
    public HazelcastInstance hazelcastInstance() {

        ClientConfig config = ClientConfig.load();
        return HazelcastClient.newHazelcastClient(config);
    }

    @PostConstruct
    public void suscribeConsumerEvent() throws Exception {

        consumerEvent.subscribe(List.of(topic), new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                consumerEvent.seekToBeginning(partitions);
            }
        });
    }

    @PostConstruct
    public void suscribeConsumerNewPassword() throws Exception {

        consumerNewPassword.subscribe(List.of("new_password"), new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                consumerNewPassword.seekToBeginning(partitions);
            }
        });
    }

}
