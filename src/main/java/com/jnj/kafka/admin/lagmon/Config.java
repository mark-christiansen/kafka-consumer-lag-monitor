package com.jnj.kafka.admin.lagmon;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class Config {

    @Bean
    @ConfigurationProperties(prefix = "admin")
    public Properties adminProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "consumer")
    public Properties consumerProperties() {
        return new Properties();
    }

    @Bean
    public AdminClient adminClient() {
        return AdminClient.create(adminProperties());
    }

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        return new KafkaConsumer<>(consumerProperties());
    }
}