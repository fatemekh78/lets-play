package com.example.secureapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdate {
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Email(message = "Email should be valid")
    private String email;
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
