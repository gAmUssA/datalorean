package com.example.datadelorean.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerEvent {
    private UUID eventId;
    private String customerId;
    private String eventType;
    private Instant timestamp;
    private String data;
    private Integer schemaVersion;

    // Default constructor for Jackson
    public CustomerEvent() {
    }

    public CustomerEvent(UUID eventId, String customerId, String eventType, String data) {
        this.eventId = eventId;
        this.customerId = customerId;
        this.eventType = eventType;
        this.timestamp = Instant.now();
        this.data = data;
        this.schemaVersion = 1;
    }

    // Getters and Setters
    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    @Override
    public String toString() {
        return "CustomerEvent{" +
                "eventId=" + eventId +
                ", customerId='" + customerId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", data='" + data + '\'' +
                ", schemaVersion=" + schemaVersion +
                '}';
    }
}