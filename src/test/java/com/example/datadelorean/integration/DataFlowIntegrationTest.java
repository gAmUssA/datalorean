package com.example.datadelorean.integration;

import com.example.datadelorean.config.TestConfig;
import com.example.datadelorean.model.CustomerEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.iceberg.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@EmbeddedKafka(partitions = 1, topics = {"customer-events", "analytical-insights"})
@Import(TestConfig.class)
class DataFlowIntegrationTest {

    @Container
    static GenericContainer<?> minioContainer = new GenericContainer<>("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data");

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StreamExecutionEnvironment env;

    @Autowired
    private Table customerEventsTable;

    private CountDownLatch latch;
    private AtomicReference<JsonNode> capturedInsight;

    @BeforeEach
    void setUp() {
        latch = new CountDownLatch(1);
        capturedInsight = new AtomicReference<>();
        System.out.println("[DEBUG_LOG] Test setup completed");
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("iceberg.s3.endpoint", 
            () -> "http://localhost:" + minioContainer.getMappedPort(9000));
        registry.add("iceberg.s3.access-key-id", () -> "minioadmin");
        registry.add("iceberg.s3.secret-access-key", () -> "minioadmin");
        System.out.println("[DEBUG_LOG] MinIO endpoint configured: http://localhost:" + 
            minioContainer.getMappedPort(9000));
    }

    @KafkaListener(topics = "analytical-insights", groupId = "test-group")
    public void listen(String message, Acknowledgment ack) throws Exception {
        System.out.println("[DEBUG_LOG] Received analytical insight: " + message);
        JsonNode insight = objectMapper.readTree(message);
        capturedInsight.set(insight);
        latch.countDown();
        ack.acknowledge();
    }

    @Test
    void testBidirectionalDataFlow() throws Exception {
        // Create a customer event
        CustomerEvent event = new CustomerEvent(
            UUID.randomUUID(),
            "TEST-CUSTOMER",
            "TEST-EVENT",
            "{\"test\":\"data\"}"
        );

        System.out.println("[DEBUG_LOG] Sending test event: " + 
            objectMapper.writeValueAsString(event));

        // Send event to Kafka
        kafkaTemplate.send("customer-events", event.getCustomerId(),
            objectMapper.writeValueAsString(event)).get(5, TimeUnit.SECONDS);

        System.out.println("[DEBUG_LOG] Event sent to Kafka, waiting for insight...");

        // Wait for the insight to be generated
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Timeout waiting for insight");

        // Verify the insight
        JsonNode insight = capturedInsight.get();
        assertNotNull(insight, "Insight should not be null");
        assertEquals("TEST-CUSTOMER", insight.get("customer_id").asText());
        assertEquals("CUSTOMER_ACTIVITY", insight.get("insight_type").asText());

        System.out.println("[DEBUG_LOG] Insight verification completed");

        // Verify Iceberg table
        assertTrue(customerEventsTable.currentSnapshot() != null, 
            "Event should be written to Iceberg");
        
        System.out.println("[DEBUG_LOG] Iceberg table verification completed");
    }
}