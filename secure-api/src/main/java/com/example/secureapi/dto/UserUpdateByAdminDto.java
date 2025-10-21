package com.example.secureapi.dto;

import com.example.secureapi.model.UserRole;
import lombok.Data;

@Data
public class UserUpdateByAdminDto {
    private String name;
    private String email;
    private String password;
    private UserRole role;
}
