package com.example.datadelorean.flink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CustomerEventsJob {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${demo.kafka.topics.customer-events}")
    private String customerEventsTopic;

    @Value("${iceberg.warehouse}")
    private String warehousePath;

    private final StreamExecutionEnvironment env;

    public CustomerEventsJob(StreamExecutionEnvironment env) {
        this.env = env;
    }

    public void startJob() throws Exception {
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create Kafka source table
        String createKafkaTable = String.format("""
            CREATE TABLE customer_events_kafka (
                event_id STRING,
                customer_id STRING,
                event_type STRING,
                `timestamp` TIMESTAMP(3),
                data STRING,
                schema_version INT,
                WATERMARK FOR `timestamp` AS `timestamp` - INTERVAL '5' SECONDS
            ) WITH (
                'connector' = 'kafka',
                'topic' = '%s',
                'properties.bootstrap.servers' = '%s',
                'properties.group.id' = 'customer-events-to-iceberg',
                'format' = 'json',
                'json.fail-on-missing-field' = 'false',
                'json.ignore-parse-errors' = 'true'
            )
            """, customerEventsTopic, bootstrapServers);

        // Create Iceberg sink table
        String createIcebergTable = String.format("""
            CREATE TABLE customer_events_iceberg (
                event_id STRING,
                customer_id STRING,
                event_type STRING,
                `timestamp` TIMESTAMP(3),
                data STRING,
                schema_version INT
            ) WITH (
                'connector' = 'iceberg',
                'catalog-type' = 'hadoop',
                'warehouse' = '%s',
                'format-version' = '2',
                'write.parquet.compression-codec' = 'snappy'
            )
            """, warehousePath);

        // Execute table creation
        tableEnv.executeSql(createKafkaTable);
        tableEnv.executeSql(createIcebergTable);

        // Transfer data from Kafka to Iceberg
        tableEnv.executeSql("""
            INSERT INTO customer_events_iceberg
            SELECT event_id, customer_id, event_type, `timestamp`, data, schema_version
            FROM customer_events_kafka
            """);
    }
}