plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_23
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Apache Flink
    implementation("org.apache.flink:flink-streaming-java:1.20.0") {
        exclude(group = "org.apache.flink", module = "flink-shaded-force-shading")
    }
    implementation("org.apache.flink:flink-clients:1.20.0")
    implementation("org.apache.flink:flink-connector-kafka:1.20.0")
    implementation("org.apache.flink:flink-json:1.20.0")
    implementation("org.apache.flink:flink-runtime:1.20.0")
    implementation("org.apache.flink:flink-table-common:1.20.0")
    implementation("org.apache.flink:flink-core:1.20.0")
    implementation("org.apache.flink:flink-connector-base:1.20.0")
    implementation("org.apache.flink:flink-connector-kafka_2.12:1.20.0")
    implementation("org.apache.flink:flink-json_2.12:1.20.0")
    implementation("org.apache.flink:flink-table-runtime:1.20.0")

    // Flink Iceberg
    implementation("org.apache.iceberg:iceberg-flink-runtime-1.20:1.4.3")
    implementation("org.apache.iceberg:iceberg-flink:1.4.3")
    implementation("org.apache.iceberg:iceberg-api:1.4.3")
    implementation("org.apache.iceberg:iceberg-common:1.4.3")
    implementation("org.apache.iceberg:iceberg-core:1.4.3")
    implementation("org.apache.iceberg:iceberg-data:1.4.3")

    // Apache Kafka
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("org.springframework.kafka:spring-kafka")

    // Apache Iceberg
    implementation("org.apache.iceberg:iceberg-core:1.4.3")
    implementation("org.apache.iceberg:iceberg-flink:1.4.3")
    implementation("org.apache.iceberg:iceberg-aws:1.4.3")

    // Hadoop and AWS
    implementation("org.apache.hadoop:hadoop-common:3.3.6")
    implementation("org.apache.hadoop:hadoop-aws:3.3.6")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.543")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:minio:1.19.3")

    // Test Runtime
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("ch.qos.logback:logback-classic")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Logging
    implementation("ch.qos.logback:logback-classic")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
