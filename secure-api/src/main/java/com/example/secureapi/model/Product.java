// src/main/java/com/example/secureapi/model/Product.java
package com.example.secureapi.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
public class Product {

    @Id
    private String id;

    @NotBlank
    private String name;

    private String description;

    @Positive
    private Double price;

    // Stores the ID of the user who owns this product
    private String userId;
}