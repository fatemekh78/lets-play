// src/main/java/com/example/secureapi/repository/ProductRepository.java
package com.example.secureapi.repository;

import com.example.secureapi.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
}