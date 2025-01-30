package com.example.datadelorean.demo;

import com.example.datadelorean.model.CustomerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class DemoScenarios {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DemoScenarios(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Scenario 1: Schema Evolution
     * Demonstrates how the system handles schema changes by introducing a new field
     */
    public void demonstrateSchemaEvolution() throws Exception {
        // Original schema event
        CustomerEvent event1 = new CustomerEvent(
            UUID.randomUUID(),
            "CUST001",
            "LOGIN",
            "{\"device\":\"mobile\"}"
        );

        // Extended schema event (new field will be ignored by old consumers)
        String extendedEvent = """
            {
                "eventId": "%s",
                "customerId": "CUST001",
                "eventType": "LOGIN",
                "timestamp": "%s",
                "data": {"device":"mobile","location":"US-NYC"},
                "schemaVersion": 2,
                "priority": "HIGH"
            }
            """.formatted(UUID.randomUUID(), java.time.Instant.now());

        // Send both events
        kafkaTemplate.send("customer-events", event1.getCustomerId(), 
            objectMapper.writeValueAsString(event1));
        TimeUnit.SECONDS.sleep(2);
        kafkaTemplate.send("customer-events", "CUST001", extendedEvent);
    }

    /**
     * Scenario 2: High-Volume Data Processing
     * Demonstrates system's ability to handle high-volume data
     */
    public void demonstrateHighVolume() throws Exception {
        CompletableFuture<?>[] futures = new CompletableFuture[1000];
        
        for (int i = 0; i < 1000; i++) {
            CustomerEvent event = new CustomerEvent(
                UUID.randomUUID(),
                "CUST" + (i % 10),
                "PAGE_VIEW",
                String.format("{\"page\":\"product-%d\"}", i)
            );
            
            futures[i] = kafkaTemplate.send("customer-events", 
                event.getCustomerId(),
                objectMapper.writeValueAsString(event))
                .toCompletableFuture();
        }
        
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
    }

    /**
     * Scenario 3: Error Handling
     * Demonstrates system's resilience to invalid data
     */
    public void demonstrateErrorHandling() throws Exception {
        // Valid event
        CustomerEvent validEvent = new CustomerEvent(
            UUID.randomUUID(),
            "CUST001",
            "PURCHASE",
            "{\"amount\":99.99}"
        );

        // Invalid JSON
        String invalidJson = "{\"eventId\":\"invalid-json";
        
        // Invalid schema
        String invalidSchema = """
            {
                "eventId": "123",
                "customerId": null,
                "eventType": "",
                "timestamp": "invalid-date"
            }
            """;

        // Send all events
        kafkaTemplate.send("customer-events", "CUST001", 
            objectMapper.writeValueAsString(validEvent));
        TimeUnit.SECONDS.sleep(1);
        kafkaTemplate.send("customer-events", "CUST001", invalidJson);
        TimeUnit.SECONDS.sleep(1);
        kafkaTemplate.send("customer-events", "CUST001", invalidSchema);
    }
}