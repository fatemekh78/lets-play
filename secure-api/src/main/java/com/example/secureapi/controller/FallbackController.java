package com.example.secureapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    // This only catches routes that don't match any other controller
    @RequestMapping("/**")
    public ResponseEntity<?> handleUnknownPaths() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", new Date());
        errorResponse.put("status", 404);
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", "The requested API endpoint was not found");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}