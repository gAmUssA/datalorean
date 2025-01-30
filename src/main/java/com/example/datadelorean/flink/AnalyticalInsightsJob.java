package com.example.datadelorean.flink;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AnalyticalInsightsJob {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${demo.kafka.topics.analytical-insights}")
    private String analyticalInsightsTopic;

    @Value("${iceberg.warehouse}")
    private String warehousePath;

    private final StreamExecutionEnvironment env;

    public AnalyticalInsightsJob(StreamExecutionEnvironment env) {
        this.env = env;
    }

    public void startJob() throws Exception {
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        // Create Kafka sink table
        String createKafkaTable = String.format("""
            CREATE TABLE analytical_insights_kafka (
                insight_id STRING,
                customer_id STRING,
                insight_type STRING,
                `timestamp` TIMESTAMP(3),
                insight_value STRING,
                confidence_score DOUBLE
            ) WITH (
                'connector' = 'kafka',
                'topic' = '%s',
                'properties.bootstrap.servers' = '%s',
                'format' = 'json'
            )
            """, analyticalInsightsTopic, bootstrapServers);

        // Create Iceberg source view with analytical query
        String createAnalyticalView = """
            CREATE VIEW customer_insights AS
            SELECT 
                CAST(UUID() AS STRING) as insight_id,
                customer_id,
                'CUSTOMER_ACTIVITY' as insight_type,
                CURRENT_TIMESTAMP as `timestamp`,
                CONCAT('{"event_count":', CAST(COUNT(*) AS STRING), 
                      ',"latest_event":"', MAX(event_type), '"}') as insight_value,
                0.95 as confidence_score
            FROM customer_events_iceberg
            GROUP BY customer_id
            """;

        // Execute table and view creation
        tableEnv.executeSql(createKafkaTable);
        tableEnv.executeSql(createAnalyticalView);

        // Transfer insights to Kafka
        tableEnv.executeSql("""
            INSERT INTO analytical_insights_kafka
            SELECT insight_id, customer_id, insight_type, `timestamp`, 
                   insight_value, confidence_score
            FROM customer_insights
            """);
    }
}