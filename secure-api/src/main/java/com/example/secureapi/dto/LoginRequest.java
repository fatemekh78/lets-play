// src/main/java/com/example/secureapi/dto/LoginRequest.java
package com.example.secureapi.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}



