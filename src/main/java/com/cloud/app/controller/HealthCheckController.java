package com.cloud.app.controller;

import com.cloud.app.service.HealthCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthCheckController {

    @Autowired
    HealthCheckService healthCheckService;
    private final Logger LOGGER = LoggerFactory.getLogger(HealthCheckService.class);
    @GetMapping("/healthz")
    public ResponseEntity<Void> isDatabaseRunning
            (HttpServletRequest payload, @RequestParam Map<String,String> queryParams){

        //400 Bad Request if payload and/or query params is present in request
        if(payload.getContentLength()>0 || !queryParams.isEmpty()){
            return ResponseEntity
                    .badRequest()
                    .header("Cache-control","no-cache, no-store, must-revalidate")
                    .header("Pragma","no-cache")
                    .header("X-Content-Type-Options","nosniff")
                    .build();
        }

        //200 Status Ok if database connectivity is successful
        if (healthCheckService.isDatabaseRunning()){
            return ResponseEntity
                    .ok()
                    .header("Cache-control","no-cache, no-store, must-revalidate")
                    .header("Pragma","no-cache")
                    .header("X-Content-Type-Options","nosniff")
                    .build();
        }
        //503 Service Unavailable if database connectivity is unsuccessful
        else {
            return ResponseEntity
                    .status(503)
                    .header("Cache-control","no-cache, no-store, must-revalidate")
                    .header("Pragma","no-cache")
                    .header("X-Content-Type-Options","nosniff")
                    .build();
        }
    }
}
