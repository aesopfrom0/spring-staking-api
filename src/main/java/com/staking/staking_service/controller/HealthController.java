package com.staking.staking_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/healthz")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }
}