// src/main/java/com/example/secureapi/controller/ProductController.java
package com.example.secureapi.controller;

import com.example.secureapi.dto.ProductResponse;
import com.example.secureapi.dto.ProductUpdate;
import com.example.secureapi.exception.GlobalExceptionHandler;
import com.example.secureapi.exception.ResourceNotFoundException;
import com.example.secureapi.model.Product;
import com.example.secureapi.model.User;
import com.example.secureapi.model.UserRole;
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

    private final ProductRepository productRepository;

    private  final UserRepository userRepository;
    @Autowired
    public ProductController(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }
    // PUBLIC: Anyone can access this
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        // Using a DTO to control what data is sent to the client
        return productRepository.findAll().stream()
                .map(product -> {
                    User user = userRepository.findById(product.getUserId()).orElseThrow(()->new ResourceNotFoundException("product without userID found!!"));
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
    @PreAuthorize("hasRole('ADMIN') or @productRepository.findById(#id).get().userId == @userRepository.findByEmail(authentication.name).get().id")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody ProductUpdate productDetails, @AuthenticationPrincipal UserDetails userDetails) {
        // 1. Find the existing product
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // 2. Verify ownership
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
        if (user.getRole() != UserRole.ROLE_ADMIN && !existingProduct.getUserId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this product");
        }
        if (productDetails == null) throw new GlobalExceptionHandler.BadRequestException("fill the product information");
        if(productDetails.getName() != null && !productDetails.getName().isBlank()){
            existingProduct.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null && !productDetails.getDescription().isBlank()){
            existingProduct.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null){
            existingProduct.setPrice(productDetails.getPrice());
        }
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
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
        product.setUserId(user.getId());
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}

