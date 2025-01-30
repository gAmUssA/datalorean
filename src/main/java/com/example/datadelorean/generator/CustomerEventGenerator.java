package com.example.datadelorean.generator;

import com.example.datadelorean.model.CustomerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.List;

@Component
public class CustomerEventGenerator {

    private static final List<String> EVENT_TYPES = List.of(
        "PAGE_VIEW",
        "ADD_TO_CART",
        "PURCHASE",
        "REVIEW",
        "LOGIN"
    );

    private static final List<String> CUSTOMER_IDS = List.of(
        "CUST001", "CUST002", "CUST003", "CUST004", "CUST005"
    );

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Random random;

    @Value("${demo.kafka.topics.customer-events}")
    private String customerEventsTopic;

    public CustomerEventGenerator(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.random = new Random();
    }

    @Scheduled(fixedRateString = "${demo.generator.interval-ms:1000}")
    public void generateEvent() {
        try {
            CustomerEvent event = new CustomerEvent(
                UUID.randomUUID(),
                CUSTOMER_IDS.get(random.nextInt(CUSTOMER_IDS.size())),
                EVENT_TYPES.get(random.nextInt(EVENT_TYPES.size())),
                generateEventData()
            );

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(customerEventsTopic, event.getCustomerId(), eventJson);
        } catch (Exception e) {
            // In production, use proper logging framework
            System.err.println("Error generating event: " + e.getMessage());
        }
    }

    private String generateEventData() {
        // Generate some random event data
        return String.format("""
            {"timestamp": %d, "session_id": "%s", "value": %.2f}
            """,
            System.currentTimeMillis(),
            UUID.randomUUID(),
            random.nextDouble() * 100.0
        );
    }
}