// src/main/java/com/example/secureapi/repository/UserRepository.java
package com.example.secureapi.repository;

import com.example.secureapi.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}