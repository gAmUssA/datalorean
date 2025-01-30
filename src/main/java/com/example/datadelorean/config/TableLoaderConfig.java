package com.example.datadelorean.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.hadoop.HadoopTableLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;

@org.springframework.context.annotation.Configuration
public class TableLoaderConfig {

    @Value("${iceberg.warehouse}")
    private String warehousePath;

    private final Configuration hadoopConfiguration;

    public TableLoaderConfig(Configuration hadoopConfiguration) {
        this.hadoopConfiguration = hadoopConfiguration;
    }

    @Bean
    public TableLoader customerEventsTableLoader() {
        String tablePath = warehousePath + "/customer_events/events";
        return new HadoopTableLoader(hadoopConfiguration, tablePath);
    }
}