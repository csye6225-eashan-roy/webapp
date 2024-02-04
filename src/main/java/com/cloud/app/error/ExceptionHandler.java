package com.cloud.app.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Void> handleMethodNotAllowedException(HttpRequestMethodNotSupportedException e) {

        //405 Method Not Allowed if anything other than GET request is sent
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("Cache-control","no-cache, no-store, must-revalidate")
                .header("Pragma","no-cache")
                .header("X-Content-Type-Options","nosniff")
                .build();
    }
}
