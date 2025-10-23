// src/main/java/com/gymsystem/common/ApiExceptionHandler.java
package com.gymsystem.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling to provide clean API error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles bean validation errors and returns a map of field -> message.
     * @param ex the exception thrown by Spring when validation fails
     * @return a 400 response with error details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>(); // Creates a map for the response body
        body.put("error", "Validation failed");
        Map<String, String> fieldErrors = new HashMap<>(); // Creates a map for field-specific errors
        ex.getBindingResult().getFieldErrors().forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage())); // Collects field errors
        body.put("fields", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // Returns 400 with the composed body
    }
}
