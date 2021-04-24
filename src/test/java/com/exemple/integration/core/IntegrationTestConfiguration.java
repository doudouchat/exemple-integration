package com.exemple.integration.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.exemple.authorization.core.client.AuthorizationClientConfiguration;
import com.exemple.service.application.core.ApplicationConfiguration;
import com.exemple.service.resource.core.ResourceConfiguration;
import com.github.nosan.boot.autoconfigure.embedded.cassandra.EmbeddedCassandraAutoConfiguration;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@Import({ ResourceConfiguration.class, ApplicationConfiguration.class, AuthorizationClientConfiguration.class })
@ComponentScan(basePackages = "com.exemple.integration")
@EnableAutoConfiguration(exclude = { CassandraAutoConfiguration.class, EmbeddedCassandraAutoConfiguration.class })
public class IntegrationTestConfiguration {

    public static final String AUTHORIZATION_URL = System.getProperty("authorization.host", "http://localhost") + ":"
            + System.getProperty("authorization.port", "8084") + "/" + System.getProperty("authorization.contextpath", "ExempleAuthorization");

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(@Value("${authorization.hazelcast.addresses}") String[] addresses) {

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(addresses);

        return HazelcastClient.newHazelcastClient(config);
    }

}
