package com.example.datadelorean.config;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@org.springframework.context.annotation.Configuration
public class FlinkConfig {

    @Value("${flink.job.checkpointing.interval}")
    private long checkpointInterval;

    @Value("${flink.job.parallelism}")
    private int parallelism;

    @Bean
    public StreamExecutionEnvironment streamExecutionEnvironment() {
        Configuration flinkConfig = new Configuration();
        
        // Enable checkpointing
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(flinkConfig);
        env.enableCheckpointing(checkpointInterval);
        env.setParallelism(parallelism);
        
        // Configure restart strategy
        env.setRestartStrategy(RestartStrategies.failureRateRestart(
            3, // max failures per interval
            Time.of(5, TimeUnit.MINUTES), // failure rate interval
            Time.of(10, TimeUnit.SECONDS) // delay between retries
        ));
        
        return env;
    }
}