// src/main/java/com/example/secureapi/exception/ErrorDetails.java
package com.example.secureapi.exception;

import java.util.Date;

public class ErrorDetails {
    private Date timestamp;
    private String message;
    private String details;
    private String validationErrors; // Optional: for validation-specific errors

    public ErrorDetails(Date timestamp, String message, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    // Additional constructor for validation errors
    public ErrorDetails(Date timestamp, String message, String validationErrors, String details) {
        this.timestamp = timestamp;
        this.message = message;
        this.validationErrors = validationErrors;
        this.details = details;
    }

    // Getters and setters
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getValidationErrors() { return validationErrors; }
    public void setValidationErrors(String validationErrors) { this.validationErrors = validationErrors; }
}