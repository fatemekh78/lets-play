// src/main/java/com/example/secureapi/dto/ProductResponse.java
package com.example.secureapi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductResponse {

    private String id;
    private String name;
    private String description;
    private Double price;
    private String userId;
    private String userName; // Added for more context

    // You can add a constructor to easily map from the Product entity
    public ProductResponse(String id, String name, String description, Double price, String userId, String userName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.userId = userId;
        this.userName = userName;
    }
}