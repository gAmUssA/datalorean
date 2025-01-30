package com.example.datadelorean.test;

import com.example.datadelorean.model.CustomerEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TestEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(TestEventProducer.class);

    @Value("${demo.kafka.topics.customer-events}")
    private String customerEventsTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public TestEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 5000)
    public void sendTestEvent() {
        try {
            CustomerEvent event = new CustomerEvent(
                UUID.randomUUID(),
                "TEST-" + UUID.randomUUID().toString().substring(0, 8),
                "TEST_EVENT",
                "{\"test\":\"data\"}"
            );

            String eventJson = objectMapper.writeValueAsString(event);
            logger.info("Sending test event: {}", eventJson);
            
            kafkaTemplate.send(customerEventsTopic, event.getCustomerId(), eventJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Test event sent successfully to topic {} at offset {}", 
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                    } else {
                        logger.error("Failed to send test event", ex);
                    }
                });
        } catch (Exception e) {
            logger.error("Error creating test event", e);
        }
    }
}