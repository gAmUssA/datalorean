package com.example.datadelorean.config;

import org.apache.iceberg.Table;
import org.apache.iceberg.Schema;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.catalog.TableIdentifier;
import org.apache.iceberg.catalog.Catalog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IcebergTableConfig {

    private final Catalog catalog;

    public IcebergTableConfig(Catalog catalog) {
        this.catalog = catalog;
    }

    @Bean
    public Table customerEventsTable() {
        TableIdentifier identifier = TableIdentifier.of("customer_events", "events");
        
        // Create table if it doesn't exist
        if (!catalog.tableExists(identifier)) {
            Schema schema = new Schema(
                Types.NestedField.required(1, "event_id", Types.StringType.get()),
                Types.NestedField.required(2, "customer_id", Types.StringType.get()),
                Types.NestedField.required(3, "event_type", Types.StringType.get()),
                Types.NestedField.required(4, "timestamp", Types.TimestampType.withoutZone()),
                Types.NestedField.required(5, "data", Types.StringType.get()),
                Types.NestedField.required(6, "schema_version", Types.IntegerType.get())
            );

            PartitionSpec spec = PartitionSpec.builderFor(schema)
                .hour("timestamp")
                .build();

            return catalog.buildTable(identifier, schema)
                .withPartitionSpec(spec)
                .withProperty("format-version", "2")
                .withProperty("write.parquet.compression-codec", "snappy")
                .create();
        }
        
        return catalog.loadTable(identifier);
    }
}