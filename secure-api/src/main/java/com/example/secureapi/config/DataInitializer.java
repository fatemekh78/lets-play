package com.example.secureapi.config;

import com.example.secureapi.model.Product;
import com.example.secureapi.model.User;
import com.example.secureapi.model.UserRole;
import com.example.secureapi.repository.ProductRepository;
import com.example.secureapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@Configuration
public class DataInitializer {

    // --- Helper Method to Create and Save Users and Products ---

    private void createIfNotExist(
            String name, String email, String password, UserRole role,
            int productCount, UserRepository userRepo, ProductRepository productRepo, PasswordEncoder encoder)
    {
        // 🔑 FIX: Check if a user with this email already exists
        Optional<User> existingUser = userRepo.findByEmail(email);

        if (existingUser.isEmpty()) {
            // 1. Create and Save the NEW User
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(encoder.encode(password));
            user.setRole(role);
            User savedUser = userRepo.save(user);

            // 2. Create and Save Products for that User
            for (int i = 1; i <= productCount; i++) {
                Product product = new Product();
                product.setName(name + "'s Product " + i);
                product.setDescription("Description for " + name + "'s Product " + i);
                product.setPrice(10.0 + (i * 5.0));
                product.setUserId(savedUser.getId());
                productRepo.save(product);
            }
            System.out.println("Created user: " + email);
        } else {
            System.out.println("Skipped user: " + email + " (Already exists)");
        }
    }

    // --- The Main Initialization Runner ---

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder)
    {
        return args -> {
            // No need for the userRepository.count() check, as the helper method handles it per user.
            System.out.println("--- Starting Data Initialization Check ---");

            // 1. Create a user with role ADMIN and two products
            createIfNotExist(
                    "Primary Admin", "admin1@example.com", "AdminPass123",
                    UserRole.ROLE_ADMIN, 2, userRepository, productRepository, passwordEncoder);

            // 2. Create another admin user and one product
            createIfNotExist(
                    "Secondary Admin", "admin2@example.com", "AdminPass456",
                    UserRole.ROLE_ADMIN, 1, userRepository, productRepository, passwordEncoder);

            // 3. Create two normal users with two products each
            createIfNotExist(
                    "Normal User A", "userA@example.com", "UserPass789",
                    UserRole.ROLE_USER, 2, userRepository, productRepository, passwordEncoder);

            createIfNotExist(
                    "Normal User B", "userB@example.com", "UserPass000",
                    UserRole.ROLE_USER, 2, userRepository, productRepository, passwordEncoder);

            System.out.println("--- Dummy Data Initialization Complete ---");
        };
    }
}