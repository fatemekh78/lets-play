package com.example.secureapi.dto;

import lombok.Data;

@Data
public class UserUpdate {
    private String name;
    private String email;
    private String password;
}
