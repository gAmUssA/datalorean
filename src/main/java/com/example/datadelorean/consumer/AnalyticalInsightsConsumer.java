package com.example.datadelorean.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;

import java.time.Instant;

@Component
public class AnalyticalInsightsConsumer {

    private final ObjectMapper objectMapper;
    private final Counter insightsCounter;

    public AnalyticalInsightsConsumer(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.insightsCounter = Counter.builder("analytical_insights_received")
            .description("Number of analytical insights received")
            .register(meterRegistry);
    }

    @KafkaListener(
        topics = "${demo.kafka.topics.analytical-insights}",
        groupId = "analytical-insights-consumer"
    )
    public void consume(String message) {
        try {
            JsonNode insight = objectMapper.readTree(message);
            
            // Extract key fields for logging
            String insightId = insight.get("insight_id").asText();
            String customerId = insight.get("customer_id").asText();
            String insightType = insight.get("insight_type").asText();
            Instant timestamp = Instant.parse(insight.get("timestamp").asText());
            double confidenceScore = insight.get("confidence_score").asDouble();
            
            // Log the insight (in production, use proper logging framework)
            System.out.printf("[DEBUG_LOG] Received insight: id=%s, customer=%s, type=%s, confidence=%.2f%n",
                insightId, customerId, insightType, confidenceScore);
            
            // Parse the insight value which is a JSON string
            JsonNode insightValue = objectMapper.readTree(insight.get("insight_value").asText());
            System.out.printf("[DEBUG_LOG] Insight details: event_count=%d, latest_event=%s%n",
                insightValue.get("event_count").asInt(),
                insightValue.get("latest_event").asText());
            
            // Increment metrics counter
            insightsCounter.increment();
            
        } catch (Exception e) {
            System.err.println("Error processing insight: " + e.getMessage());
        }
    }
}