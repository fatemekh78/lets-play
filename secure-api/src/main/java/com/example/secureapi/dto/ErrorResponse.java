// src/main/java/com/example/secureapi/dto/ErrorResponse.java
package com.example.secureapi.dto;

public class ErrorResponse {
    private final String message;
    private final String details;
    private final int status;
    private final long timestamp = System.currentTimeMillis();

    public ErrorResponse(String message, String details, int status) {
        this.message = message;
        this.details = details;
        this.status = status;
    }

    // --- Getters ---

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public int getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}