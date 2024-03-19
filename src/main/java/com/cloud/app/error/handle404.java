package com.cloud.app.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class handle404 {

    private static final Logger LOGGER = LoggerFactory.getLogger(handle404.class);

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity handleNoHandlerFoundException(NoHandlerFoundException ex, WebRequest request) {
        LOGGER.warn("404 Not Found: {}", request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("There was an unexpected error (type=Not Found, status=404).");
    }
}
