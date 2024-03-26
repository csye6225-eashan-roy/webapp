package com.cloud.app.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class EmailVerificationErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerificationErrorHandler.class);

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<?> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        LOGGER.warn("Email verification required: {}", ex.getMessage());
        // You can customize the response body or structure as needed
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ex.getMessage());
    }
}

