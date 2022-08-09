package com.exemple.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

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

}
