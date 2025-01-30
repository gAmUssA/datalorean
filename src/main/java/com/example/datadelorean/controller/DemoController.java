package com.example.datadelorean.controller;

import com.example.datadelorean.demo.DemoScenarios;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoScenarios demoScenarios;

    public DemoController(DemoScenarios demoScenarios) {
        this.demoScenarios = demoScenarios;
    }

    @PostMapping("/schema-evolution")
    public ResponseEntity<String> demonstrateSchemaEvolution() {
        try {
            demoScenarios.demonstrateSchemaEvolution();
            return ResponseEntity.ok("Schema evolution demo completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during schema evolution demo: " + e.getMessage());
        }
    }

    @PostMapping("/high-volume")
    public ResponseEntity<String> demonstrateHighVolume() {
        try {
            demoScenarios.demonstrateHighVolume();
            return ResponseEntity.ok("High volume processing demo completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during high volume demo: " + e.getMessage());
        }
    }

    @PostMapping("/error-handling")
    public ResponseEntity<String> demonstrateErrorHandling() {
        try {
            demoScenarios.demonstrateErrorHandling();
            return ResponseEntity.ok("Error handling demo completed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Error during error handling demo: " + e.getMessage());
        }
    }
}