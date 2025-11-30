// src/main/java/com/example/secureapi/controller/AuthController.java
package com.example.secureapi.controller;

import com.example.secureapi.dto.LoginRequest;
import com.example.secureapi.model.User;
import com.example.secureapi.model.UserRole;
import com.example.secureapi.repository.UserRepository;
import com.example.secureapi.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Create JWT token and add to cookie
            Cookie jwtCookie = jwtTokenProvider.generateJwtCookie(authentication);
            response.addCookie(jwtCookie);

            return ResponseEntity.ok("Logged in successfully");

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) {
        log.info("Received registration request: {}", user);
        try {
            // Check if email already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return new ResponseEntity<>("Email address already in use!", HttpStatus.BAD_REQUEST);
            }

            // Validate required fields (additional safety)
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return new ResponseEntity<>("Name is required", HttpStatus.BAD_REQUEST);
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            }

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
            }

            // Hash password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // Default role
            user.setRole(UserRole.ROLE_USER);

            User savedUser = userRepository.save(user);
            log.info("User saved to DB: {}", savedUser);
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);

        } catch (Exception e) {
            log.error("Registration failed", e);
            return new ResponseEntity<>("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        try {
            Cookie cookie = jwtTokenProvider.createLogoutCookie();
            response.addCookie(cookie);
            return ResponseEntity.ok("Logout successful");
        } catch (Exception e) {
            return new ResponseEntity<>("Logout failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}