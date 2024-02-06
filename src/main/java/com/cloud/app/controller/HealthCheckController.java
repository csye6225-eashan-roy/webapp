package com.cloud.app.controller;

import com.cloud.app.service.HealthCheckService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // Other health endpoint methods returning 405 Method Not Allowed
    @RequestMapping(
            path = "/healthz",
            method = {RequestMethod.POST, RequestMethod.PUT,
                    RequestMethod.DELETE, RequestMethod.HEAD,
                    RequestMethod.OPTIONS, RequestMethod.PATCH},
            produces = "application/json"
    )
    public ResponseEntity<?> methodNotAllowed() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-control","no-cache, no-store, must-revalidate")
                .header("Pragma","no-cache")
                .header("X-Content-Type-Options","nosniff")
                .build();
    }
}
