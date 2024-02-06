package com.cloud.app.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class handle405 {
    @org.springframework.web.bind.annotation.ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-control","no-cache, no-store, must-revalidate")
                .header("Pragma","no-cache")
                .header("X-Content-Type-Options","nosniff")
                .build();
    }
}
