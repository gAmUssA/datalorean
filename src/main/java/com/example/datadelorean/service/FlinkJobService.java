package com.example.datadelorean.service;

import com.example.datadelorean.model.CustomerEvent;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.json.JsonDeserializationSchema;
import org.apache.flink.formats.json.JsonSerializationSchema;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class FlinkJobService {

  private static final Logger logger = LoggerFactory.getLogger(FlinkJobService.class);

  private final StreamExecutionEnvironment env;
  private final ExecutorService executorService;
  private CompletableFuture<Void> jobFuture;

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${demo.kafka.topics.customer-events}")
  private String customerEventsTopic;

  @Value("${spring.kafka.consumer.group-id}")
  private String consumerGroupId;

  @Value("${demo.kafka.topics.dead-letter-queue:customer-events-dlq}")
  private String dlqTopic;

  private static final OutputTag<CustomerEvent> FAILED_EVENTS =
      new OutputTag<CustomerEvent>("failed-events") {
      };

  private DataStream<CustomerEvent> configureJob() {
    logger.info("Configuring Kafka source with bootstrap servers: {}, topic: {}, group: {}",
                bootstrapServers, customerEventsTopic, consumerGroupId);

    // Configure Kafka source
    KafkaSource<CustomerEvent> source = KafkaSource.<CustomerEvent>builder()
        .setBootstrapServers(bootstrapServers)
        .setTopics(customerEventsTopic)
        .setGroupId(consumerGroupId)
        .setStartingOffsets(OffsetsInitializer.earliest())
        .setValueOnlyDeserializer(new JsonDeserializationSchema<>(CustomerEvent.class))
        .setProperty("enable.auto.commit", "false")
        .setProperty("auto.offset.reset", "earliest")
        .build();

    // Create the data stream with watermark strategy
    return env.fromSource(
        source,
        WatermarkStrategy.forMonotonousTimestamps(),
        "Customer Events Kafka Source"
    );
  }

  @Autowired
  public FlinkJobService(StreamExecutionEnvironment env) {
    this.env = env;
    this.executorService = Executors.newSingleThreadExecutor();
  }

  @PostConstruct
  public void startJob() {
    logger.info("Starting Flink job...");
    jobFuture = CompletableFuture.runAsync(() -> {
      try {
        // Configure and submit the job
        DataStream<CustomerEvent> stream = configureJob();

        // Configure Kafka sink for DLQ
        logger.info("Configuring DLQ sink for topic: {}", dlqTopic);
        KafkaSink<CustomerEvent> dlqSink;
        try {
          dlqSink = KafkaSink.<CustomerEvent>builder()
              .setBootstrapServers(bootstrapServers)
              .setRecordSerializer(KafkaRecordSerializationSchema.<CustomerEvent>builder()
                                       .setTopic(dlqTopic)
                                       .setValueSerializationSchema(new JsonSerializationSchema<CustomerEvent>())
                                       .build())
              .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
              .setProperty("transaction.timeout.ms", "300000")
              .build();
          logger.info("DLQ sink configured successfully");
        } catch (Exception e) {
          logger.error("Failed to configure DLQ sink", e);
          throw new RuntimeException("Failed to configure DLQ sink", e);
        }

        // Add transformations with error handling, metrics, and DLQ
        SingleOutputStreamOperator<CustomerEvent> processedStream = stream
            .keyBy(CustomerEvent::getCustomerId)
            .process(new KeyedProcessFunction<String, CustomerEvent, CustomerEvent>() {
              private transient Counter processedEvents;
              private transient Counter failedEvents;

              @Override
              public void open(Configuration parameters) throws Exception {
                RuntimeContext context = getRuntimeContext();
                processedEvents = context.getMetricGroup().counter("processedEvents");
                failedEvents = context.getMetricGroup().counter("failedEvents");
              }

              @Override
              public void processElement(
                  CustomerEvent event,
                  KeyedProcessFunction<String, CustomerEvent, CustomerEvent>.Context ctx,
                  Collector<CustomerEvent> out) throws Exception {
                try {
                  logger.debug("Processing event: {}", event);
                  processedEvents.inc();
                  out.collect(event);
                } catch (Exception e) {
                  logger.error("Error processing event: {}", event, e);
                  failedEvents.inc();
                  ctx.output(FAILED_EVENTS, event);
                }
              }
            })
            .name("Event Processing");

        // Process and log failed events
        DataStream<CustomerEvent> failedEvents = processedStream.getSideOutput(FAILED_EVENTS);

        // Add logging for failed events
        failedEvents
            .map(event -> {
              logger.info("Sending failed event to DLQ: {}", event);
              return event;
            })
            .name("DLQ Event Logging")
            .sinkTo(dlqSink)
            .name("Failed Events DLQ");

        // Continue with main stream processing
        processedStream
            .map(event -> {
              logger.debug("Successfully processed event: {}", event);
              return event;
            })
            .name("Successful Event Logging")
            .print(); // For debugging purposes

        logger.info("Job pipeline configured successfully");

        env.execute("Data DeLorean Job");
        logger.info("Flink job started successfully");
      } catch (Exception e) {
        logger.error("Failed to start Flink job", e);
        throw new RuntimeException("Failed to start Flink job", e);
      }
    }, executorService);

    jobFuture.exceptionally(throwable -> {
      logger.error("Flink job failed: {}", throwable.getMessage(), throwable);
      return null;
    });
  }

  @PreDestroy
  public void stopJob() {
    logger.info("Stopping Flink job...");
    try {
      if (jobFuture != null) {
        try {
          env.close();
          logger.info("Flink environment closed successfully");
        } catch (Exception e) {
          logger.error("Error closing Flink environment: {}", e.getMessage(), e);
        }
        jobFuture.cancel(true);
        logger.info("Job future cancelled");
      }
    } finally {
      executorService.shutdownNow();
      logger.info("Executor service shut down");
    }
  }
}
