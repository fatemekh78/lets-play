package com.example.secureapi.dto;

import com.example.secureapi.model.UserRole;
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
    // Password, based on this definition, is mandatory for any update
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private UserRole role;
}
