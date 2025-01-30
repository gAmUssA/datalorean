package com.example.datadelorean.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.Table;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.types.Types.NestedField;
import org.apache.iceberg.types.Types.StructType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IcebergTestConfig {

    @Value("${iceberg.warehouse}")
    private String warehousePath;

    @Bean
    @Primary
    public Table customerEventsTable(Configuration hadoopConfiguration) {
        HadoopCatalog catalog = new HadoopCatalog(hadoopConfiguration, warehousePath);
        TableIdentifier tableId = TableIdentifier.of("customer_events", "events");

        // Create table if it doesn't exist
        if (!catalog.tableExists(tableId)) {
            return catalog.createTable(tableId,
                new Schema(
                    NestedField.required(1, "event_id", Types.UUIDType.get()),
                    NestedField.required(2, "customer_id", Types.StringType.get()),
                    NestedField.required(3, "event_type", Types.StringType.get()),
                    NestedField.required(4, "event_data", Types.StringType.get())
                ));
        }

        return catalog.loadTable(tableId);
    }
}
