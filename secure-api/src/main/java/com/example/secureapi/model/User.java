// src/main/java/com/example/secureapi/model/User.java
package com.example.secureapi.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Data // Lombok annotation for getters, setters, toString, etc.
public class User {

    @Id
    private String id;

    @NotBlank
    @Size(min = 3, max = 20)
    private String name;

    @NotBlank
    @Email
    @Indexed(unique = true) // Ensures email is unique in the database
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password; // This will store the hashed password

    private UserRole role;
}