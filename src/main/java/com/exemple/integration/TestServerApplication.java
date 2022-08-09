package com.exemple.integration;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import kafka.server.KafkaConfig;

@SpringBootApplication
public class TestServerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TestServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TestServerApplication.class, args);
    }

    @Configuration
    @ConditionalOnProperty(value = "port", prefix = "test.hazelcast")
    public class HazelcastConfiguration {

        @Bean
        public HazelcastInstance hazelcastInstance(@Value("${test.hazelcast.port}") int port) {

            Config config = new Config();
            config.getNetworkConfig().setPort(port);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
            config.getUserCodeDeploymentConfig().setEnabled(true);

            LOG.info("STARTING EMBEDDED HAZELCAST");

            return Hazelcast.newHazelcastInstance(config);
        }

    }

    @Configuration
    @ConditionalOnProperty(value = { "port", "dir", "defaultTopic" }, prefix = "test.kafka")
    public class KafkaConfiguration {

        private final int kafkaPort;

        private final String logDir;

        private final String defaultTopic;

        public KafkaConfiguration(@Value("${test.kafka.port}") int kafkaPort, @Value("${test.kafka.dir}") String logDir,
                @Value("${test.kafka.defaultTopic}") String defaultTopic) {
            this.kafkaPort = kafkaPort;
            this.logDir = logDir;
            this.defaultTopic = defaultTopic;

        }

        @Bean(destroyMethod = "destroy")
        public EmbeddedKafkaBroker embeddedKafka() {

            EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, false, defaultTopic).brokerProperty(KafkaConfig.LogDirsProp(),
                    logDir + "/" + UUID.randomUUID());
            embeddedKafka.kafkaPorts(kafkaPort);

            LOG.info("STARTING EMBEDDED KAFKA");

            return embeddedKafka;
        }

    }

}
