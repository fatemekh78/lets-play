// src/main/java/com/example/secureapi/dto/UserResponse.java
package com.example.secureapi.dto;

import com.example.secureapi.model.User; // Assuming your User entity is here
import lombok.Data;
import lombok.NoArgsConstructor; // Recommended for DTOs
import lombok.AllArgsConstructor; // Recommended for DTOs

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id; // Crucial: Users need an ID for clients to reference them
    private String name;
    private String email;
    private String role;

    // Recommended: Constructor to easily map from the User entity
    public UserResponse(User user) {
        this.id = Long.valueOf(user.getId());
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole().name(); // Assuming role is an Enum
    }
}