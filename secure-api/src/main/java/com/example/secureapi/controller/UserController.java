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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final  JwtTokenProvider jwtTokenProvider;
    private final ProductRepository productRepository;

@Autowired
    public UserController(ProductRepository productRepository,UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.productRepository = productRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok().body(userRepository.findAll());
    }
    @PutMapping
    public ResponseEntity<String> updateInfo(@AuthenticationPrincipal UserDetails user,@RequestBody UserUpdate userUpdate, HttpServletResponse response) {
        User loggedInUser = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("wrong user logged in"));
        if(!userUpdate.getName().isBlank()) {
            loggedInUser.setName(userUpdate.getName());
        }
        if(!userUpdate.getEmail().isBlank()) {
            loggedInUser.setEmail(userUpdate.getEmail());
        }
        if(!userUpdate.getPassword().isBlank()) {
            loggedInUser.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }
        response.addCookie(jwtTokenProvider.saveJwtInCookie(loggedInUser.getEmail(), loggedInUser.getPassword()));
        return ResponseEntity.ok("updated successfully");
    }
    @DeleteMapping
    public ResponseEntity<String> deleteInfo(@AuthenticationPrincipal UserDetails user, HttpServletResponse response) {
        User loggedInUser = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("wrong user logged in"));
        List<Product> userProducts = productRepository.findByUserId(loggedInUser.getId());
        if(!userProducts.isEmpty()) {
            productRepository.deleteAll(userProducts);
        }
        userRepository.deleteById(loggedInUser.getId());
        Cookie cookie = jwtTokenProvider.createCookie(null,0);
        response.addCookie(cookie);
        return ResponseEntity.ok("deleted successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails user) {
        User loggedInUser = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("wrong user logged in"));
        UserResponse userResponse = new UserResponse();
        userResponse.setEmail(loggedInUser.getEmail());
        userResponse.setName(loggedInUser.getName());
        userResponse.setRole(loggedInUser.getRole().toString());
        return ResponseEntity.ok().body(userResponse);
    }
    @PutMapping("/me")
    public ResponseEntity<String> updateMyInfo(@AuthenticationPrincipal UserDetails user,@RequestBody UserUpdate userUpdate, HttpServletResponse response) {
        User loggedInUser = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("wrong user logged in"));
        if(!userUpdate.getName().isBlank()) {
            loggedInUser.setName(userUpdate.getName());
        }
        if(!userUpdate.getEmail().isBlank()) {
            loggedInUser.setEmail(userUpdate.getEmail());
        }
        if(!userUpdate.getPassword().isBlank()) {
            loggedInUser.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }
        response.addCookie(jwtTokenProvider.saveJwtInCookie(loggedInUser.getEmail(), loggedInUser.getPassword()));
        return ResponseEntity.ok("updated successfully");
    }
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyInfo(@AuthenticationPrincipal UserDetails user, HttpServletResponse response) {
        User loggedInUser = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("wrong user logged in"));
        List<Product> userProducts = productRepository.findByUserId(loggedInUser.getId());
        if(!userProducts.isEmpty()) {
            productRepository.deleteAll(userProducts);
        }
        userRepository.deleteById(loggedInUser.getId());
        Cookie cookie = jwtTokenProvider.createCookie(null,0);
        response.addCookie(cookie);
        return ResponseEntity.ok("deleted successfully");
    }


}
