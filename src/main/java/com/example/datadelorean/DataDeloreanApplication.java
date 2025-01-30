package com.example.datadelorean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DataDeloreanApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataDeloreanApplication.class, args);
    }
}