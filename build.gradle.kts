plugins {
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven { url = uri("https://packages.confluent.io/maven/") }
    maven { url = uri("https://repository.apache.org/content/repositories/snapshots/") }
    maven { url = uri("https://repository.apache.org/content/repositories/releases/") }
    maven { url = uri("https://repo.maven.apache.org/maven2") }
    maven { url = uri("https://packages.iceberg.apache.org/maven") }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Apache Flink
    implementation("org.apache.flink:flink-streaming-java:1.20.0") {
        exclude(group = "org.apache.flink", module = "flink-shaded-force-shading")
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
        exclude(group = "log4j", module = "log4j")
        exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    }
    implementation("org.apache.flink:flink-clients:1.20.0") {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
    }
    implementation("org.apache.flink:flink-connector-kafka:3.4.0-1.20")
    implementation("org.apache.flink:flink-json:1.20.0")
    implementation("org.apache.flink:flink-runtime:1.20.0") {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
    }
    implementation("org.apache.flink:flink-table-common:1.20.0")
    implementation("org.apache.flink:flink-core:1.20.0")
    implementation("org.apache.flink:flink-connector-base:1.20.0")
    implementation("org.apache.flink:flink-table-api-java:1.20.0")
    implementation("org.apache.flink:flink-table-api-java-bridge:1.20.0")
    implementation("org.apache.flink:flink-table-runtime:1.20.0")

    // Apache Iceberg and Flink Integration
    implementation("org.apache.iceberg:iceberg-core:1.7.1")
    implementation("org.apache.iceberg:iceberg-api:1.7.1")
    implementation("org.apache.iceberg:iceberg-common:1.7.1")
    implementation("org.apache.iceberg:iceberg-data:1.7.1")
    implementation("org.apache.iceberg:iceberg-aws:1.7.1")
    implementation("org.apache.iceberg:iceberg-bundled-guava:1.7.1")
    implementation("org.apache.iceberg:iceberg-parquet:1.7.1")
    implementation("org.apache.iceberg:iceberg-flink:1.7.1")
    implementation("org.apache.iceberg:iceberg-flink-runtime-1.20:1.7.1")

    // Apache Kafka
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("org.springframework.kafka:spring-kafka")

    // Hadoop and AWS
    implementation("org.apache.hadoop:hadoop-common:3.3.6") {
        exclude(group = "org.slf4j", module = "slf4j-reload4j")
        exclude(group = "log4j", module = "log4j")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.apache.hadoop:hadoop-aws:3.3.6") {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("software.amazon.awssdk:s3:2.20.68")
    implementation("software.amazon.awssdk:sts:2.20.68")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:minio:1.19.3")

    // Test Runtime
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Logging
    implementation("ch.qos.logback:logback-classic")
    implementation("org.slf4j:jcl-over-slf4j:2.0.9")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
