package com.example.secureapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
@Data
public class ProductUpdate {
        private String name;
        private String description;
        @Positive
        private Double price;
}
