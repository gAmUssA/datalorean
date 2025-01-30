package com.example.datadelorean.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.iceberg.catalog.Catalog;
import org.apache.iceberg.flink.TableLoader;
import org.apache.iceberg.aws.s3.S3FileIO;
import org.apache.iceberg.io.FileIO;
import org.apache.iceberg.hadoop.SerializableConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import java.net.URI;
import org.apache.iceberg.util.SerializableSupplier;

@org.springframework.context.annotation.Configuration
public class TableLoaderConfig {

    @Value("${iceberg.warehouse}")
    private String warehousePath;

    @Value("${iceberg.s3.endpoint}")
    private String s3Endpoint;

    @Value("${iceberg.s3.access-key-id}")
    private String accessKeyId;

    @Value("${iceberg.s3.secret-access-key}")
    private String secretAccessKey;

    @Value("${iceberg.s3.path-style-access}")
    private boolean pathStyleAccess;

    private final Configuration hadoopConfiguration;

    public TableLoaderConfig(Configuration hadoopConfiguration) {
        this.hadoopConfiguration = hadoopConfiguration;
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .endpointOverride(URI.create(s3Endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .region(Region.US_EAST_1)
            .build();
    }

    @Bean
    public FileIO s3FileIO(S3Client s3Client) {
        return new S3FileIO(() -> s3Client);
    }

    @Bean
    public TableLoader customerEventsTableLoader(FileIO fileIO) {
        String tablePath = warehousePath + "/customer_events/events";
        hadoopConfiguration.set("fs.s3a.endpoint", s3Endpoint);
        hadoopConfiguration.set("fs.s3a.access.key", accessKeyId);
        hadoopConfiguration.set("fs.s3a.secret.key", secretAccessKey);
        hadoopConfiguration.set("fs.s3a.path.style.access", String.valueOf(pathStyleAccess));

        return TableLoader.fromHadoopTable(tablePath, hadoopConfiguration);
    }
}
