// src/main/java/com/example/secureapi/controller/ProductController.java
package com.example.secureapi.controller;

import com.example.secureapi.dto.ProductResponse; // Assuming you have a DTO
import com.example.secureapi.exception.GlobalExceptionHandler;
import com.example.secureapi.exception.ResourceNotFoundException;
import com.example.secureapi.model.Product;
import com.example.secureapi.model.User;
import com.example.secureapi.repository.ProductRepository;
import com.example.secureapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // PUBLIC: Anyone can access this
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        // Using a DTO to control what data is sent to the client
        return productRepository.findAll().stream()
                .map(product -> {
                    User user = userRepository.findById(product.getUserId()).orElse(null);
                    String userName = (user != null) ? user.getName() : "Unknown";
                    return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getUserId(), userName);
                })
                .collect(Collectors.toList());
    }
    @GetMapping("/my-products")
    public ResponseEntity<List<ProductResponse>> getMyProducts(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Get current authenticated user directly from repository
            String username = userDetails.getUsername();
            User currentUser = userRepository.findByEmail(username)
                    .orElseThrow(() -> new GlobalExceptionHandler.BadRequestException("User not found"));

            // Get products for current user directly from repository
            List<Product> products = productRepository.findByUserId(currentUser.getId());

            // Convert to DTO/Response
            List<ProductResponse> productResponses = products.stream()
                    .map(product -> new ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            currentUser.getId(),
                            currentUser.getName()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productResponses);

        } catch (GlobalExceptionHandler.BadRequestException e) {
            // This will be caught by GlobalExceptionHandler
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
    }

    // OWNER: Only the user who created the product can update it
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Added security annotation
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product productDetails, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Find the existing product
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // 2. Verify ownership
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
        if (!existingProduct.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this product");
        }

        // 3. Update fields and save
        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        Product updatedProduct = productRepository.save(existingProduct);
        return ResponseEntity.ok(updatedProduct);
    }

    // ADMIN or OWNER: Only an admin or the user who created the product can delete it
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @productRepository.findById(#id).get().userId == @userRepository.findByEmail(authentication.name).get().id")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    // AUTHENTICATED USER: Any authenticated user can create a product
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Added security annotation
    public ResponseEntity<Product> createProduct(@RequestBody Product product, @AuthenticationPrincipal UserDetails userDetails) {
        UserRepository userRepository = null;
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
        product.setUserId(user.getId());
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}

