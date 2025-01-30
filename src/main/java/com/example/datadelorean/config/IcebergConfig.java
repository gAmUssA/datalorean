package com.example.datadelorean.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.hadoop.HadoopCatalog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class IcebergConfig {

    @Value("${iceberg.warehouse}")
    private String warehousePath;

    @Value("${iceberg.s3.endpoint}")
    private String s3Endpoint;

    @Value("${iceberg.s3.access-key-id}")
    private String accessKeyId;

    @Value("${iceberg.s3.secret-access-key}")
    private String secretAccessKey;

    @Bean
    public Configuration hadoopConfiguration() {
        Configuration conf = new Configuration();
        
        // S3 configuration
        conf.set("fs.s3a.endpoint", s3Endpoint);
        conf.set("fs.s3a.access.key", accessKeyId);
        conf.set("fs.s3a.secret.key", secretAccessKey);
        conf.set("fs.s3a.path.style.access", "true");
        conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
        
        // AWS SDK configuration
        conf.set("fs.s3a.aws.credentials.provider", 
                "org.apache.hadoop.fs.s3a.SimpleAWSCredentialsProvider");
        
        return conf;
    }

    @Bean
    public Catalog icebergCatalog(Configuration hadoopConfiguration) {
        return new HadoopCatalog(hadoopConfiguration, warehousePath);
    }
}