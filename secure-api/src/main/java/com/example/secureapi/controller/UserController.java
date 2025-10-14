package com.example.secureapi.controller;

import com.example.secureapi.dto.UserResponse;
import com.example.secureapi.dto.UserUpdate;
import com.example.secureapi.exception.ResourceNotFoundException;
import com.example.secureapi.model.Product;
import com.example.secureapi.model.User;
import com.example.secureapi.repository.ProductRepository;
import com.example.secureapi.repository.UserRepository;
import com.example.secureapi.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProductRepository productRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserController(ProductRepository productRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.productRepository = productRepository;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userRepository.findAll());
    }

    @PutMapping
    public ResponseEntity<String> updateInfo(@AuthenticationPrincipal UserDetails userDetails,
                                             @Valid @RequestBody UserUpdate userUpdate,
                                             HttpServletResponse response) {
        User loggedInUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean needsNewToken = false;
        String newEmail = null;
        String newPassword = null;

        // Update name
        if(userUpdate.getName() != null && !userUpdate.getName().isBlank()) {
            loggedInUser.setName(userUpdate.getName().trim());
        }

        // Update email
        if(userUpdate.getEmail() != null && !userUpdate.getEmail().isBlank()) {
            String updatedEmail = userUpdate.getEmail().trim();
            // Check if email is already taken by another user
            if(!updatedEmail.equals(loggedInUser.getEmail()) &&
                    userRepository.findByEmail(updatedEmail).isPresent()) {
                return ResponseEntity.badRequest().body("Email is already in use");
            }
            loggedInUser.setEmail(updatedEmail);
            newEmail = updatedEmail;
            needsNewToken = true;
        }

        // Update password
        if(userUpdate.getPassword() != null && !userUpdate.getPassword().isBlank()) {
            String newPasswordPlain = userUpdate.getPassword().trim();
            loggedInUser.setPassword(passwordEncoder.encode(newPasswordPlain));
            newPassword = newPasswordPlain;
            needsNewToken = true;
        }

        // Save updated user
        userRepository.save(loggedInUser);

        // Generate new token if email or password was changed
        if(needsNewToken) {
            try {
                // Re-authenticate with new credentials
                String emailToUse = newEmail != null ? newEmail : loggedInUser.getEmail();
                String passwordToUse = newPassword != null ? newPassword : userUpdate.getPassword();

                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(emailToUse, passwordToUse)
                );

                Cookie newJwtCookie = jwtTokenProvider.generateJwtCookie(authentication);
                response.addCookie(newJwtCookie);
            } catch (Exception e) {
                // If authentication fails, still return success but log the issue
                System.err.println("Failed to generate new JWT token: " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Updated successfully");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteInfo(@AuthenticationPrincipal UserDetails userDetails,
                                             HttpServletResponse response) {
        User loggedInUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Delete user's products
        List<Product> userProducts = productRepository.findByUserId(loggedInUser.getId());
        if(!userProducts.isEmpty()) {
            productRepository.deleteAll(userProducts);
        }

        // Delete user
        userRepository.deleteById(loggedInUser.getId());

        // Clear JWT cookie
        Cookie logoutCookie = jwtTokenProvider.createLogoutCookie();
        response.addCookie(logoutCookie);

        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User loggedInUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(loggedInUser.getEmail());
        userResponse.setName(loggedInUser.getName());
        userResponse.setRole(loggedInUser.getRole().toString());

        return ResponseEntity.ok().body(userResponse);
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyInfo(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody UserUpdate userUpdate,
                                               HttpServletResponse response) {
        // Reuse the same logic as the general update method
        return updateInfo(userDetails, userUpdate, response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyInfo(@AuthenticationPrincipal UserDetails userDetails,
                                               HttpServletResponse response) {
        // Reuse the same logic as the general delete method
        return deleteInfo(userDetails, response);
    }
}