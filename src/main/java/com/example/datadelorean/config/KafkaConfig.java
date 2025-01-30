package com.example.datadelorean.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${demo.kafka.topics.customer-events}")
    private String customerEventsTopic;

    @Value("${demo.kafka.topics.analytical-insights}")
    private String analyticalInsightsTopic;

    @Bean
    public NewTopic customerEventsTopic() {
        return TopicBuilder.name(customerEventsTopic)
                .partitions(1)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "604800000" // 7 days
                ))
                .build();
    }

    @Bean
    public NewTopic analyticalInsightsTopic() {
        return TopicBuilder.name(analyticalInsightsTopic)
                .partitions(1)
                .replicas(1)
                .configs(Map.of(
                        "cleanup.policy", "delete",
                        "retention.ms", "604800000" // 7 days
                ))
                .build();
    }
}