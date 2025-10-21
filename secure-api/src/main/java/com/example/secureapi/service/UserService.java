package com.example.secureapi.service;
//
//import com.example.secureapi.dto.UserUpdate;
//import com.example.secureapi.dto.UserUpdateByAdminDto;
//import com.example.secureapi.exception.ResourceNotFoundException;
//import com.example.secureapi.model.*;
//import com.example.secureapi.repository.ProductRepository;
//import com.example.secureapi.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//@Service
public class UserService {}
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final ProductRepository productRepository;
//    // Dependencies are injected through the constructor
//    @Autowired
//    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ProductRepository productRepository) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.productRepository = productRepository;
//    }
//
//    public User registerUser(User user) {
//        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
//            throw new IllegalStateException("Email already in use: " + user.getEmail());
//        });
//
//        String hashedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(hashedPassword);
//        if(user.getRole() == null){
//            user.setRole(UserRole. ROLE_USER);
//        }
//        return userRepository.save(user);
//    }
//    public User registerUserByAdmin(User user) {
//        userRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
//            throw new IllegalStateException("Email already in use: " + user.getEmail());
//        });
//
//        String hashedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(hashedPassword);
//
//        return userRepository.save(user);
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        // This returns your actual User object, which is also a UserDetails
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
//    }
//
//
//    public List<User> getAllUsers() {
//        return userRepository.findAll();
//    }
//
//    public User getUserById(String id) {
//        return userRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
//    }
//
//    public User getUserByEmail(String email) {
//        return userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
//    }
//
//
//    public User authUser(UserDetails userDetails) {
//        String userEmail = userDetails.getUsername();
//        return userRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
//    }
//
////    public boolean adminCheck(UserDetails currentUser){
////        return currentUser.getAuthorities().stream()
////                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.name()));
////    }
//
//    public boolean isEmailAvailable(String newEmail, String currentUserId) {
//        Optional<User> userWithNewEmail = userRepository.findByEmail(newEmail);
//
//        if (userWithNewEmail.isEmpty()) {
//            return true;
//        }
//
//        return userWithNewEmail.get().getId().equals(currentUserId);
//    }
//
//
//    public User updateUserByAdmin(String ID, UserUpdateByAdminDto updateDto) {
//        User userToUpdate = userRepository.findById(ID)
//                .orElseThrow(() -> new BadRequestException("No user found with ID: " + ID ));
//
//        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
//            userToUpdate.setName(updateDto.getName());
//        }
//        if (updateDto.getEmail() != null && !updateDto.getEmail().isBlank()) {
//            userToUpdate.setEmail(updateDto.getEmail());
//        }
//        if (updateDto.getRole() != null) {
//            userToUpdate.setRole(updateDto.getRole());
//        }
//
//        return userRepository.save(userToUpdate);
//    }
//
//    /**
//     * Deletes the currently authenticated user and all of their products.
//     */
//    public void deleteCurrentUser(User currentUser) {
//        // delete all products associated with this user
//        List<Product> productsToDelete = productRepository.findByUserId(currentUser.getId());
//        if (!productsToDelete.isEmpty()) {
//            productRepository.deleteAll(productsToDelete);
//        }
//
//        userRepository.deleteById(currentUser.getId());
//    }
//
//    /**
//     * Deletes a user by their ID, intended for admin use.
//     * Also deletes all products owned by that user.
//     */
//    @PreAuthorize("hasAuthority('ADMIN')")
//    public void deleteUserByAdmin(String userIdToDelete) {
//        // Verify user exists before proceeding
//        if (!userRepository.existsById(userIdToDelete)) {
//            throw new ResourceNotFoundException("User not found with ID: " + userIdToDelete);
//        }
//
//        // Delete associated products
//        List<Product> productsToDelete = productRepository.findByUserId(userIdToDelete);
//        if (!productsToDelete.isEmpty()) {
//            productRepository.deleteAll(productsToDelete);
//        }
//
//        userRepository.deleteById(userIdToDelete);
//    }
//    public record UserUpdateResult(User updatedUser, boolean tokenInvalidated) {}
//
//    public UserUpdateResult updateUser(UserUpdate userUpdateDto, User currentUser) {
//        boolean tokenInvalidated = false;
//
//        if (userUpdateDto.getName() != null && !userUpdateDto.getName().isBlank()) {
//            currentUser.setName(userUpdateDto.getName());
//        }
//
//        if (userUpdateDto.getPassword() != null && !userUpdateDto.getPassword().isBlank()) {
//            String hashedPassword = passwordEncoder.encode(userUpdateDto.getPassword());
//            currentUser.setPassword(hashedPassword);
//            tokenInvalidated = true; // Password change invalidates old token
//        }
//
//        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().isBlank() && !userUpdateDto.getEmail().equals(currentUser.getEmail())) {
//            // Check if the new email is already taken by ANOTHER user.
//            Optional<User> userWithNewEmail = userRepository.findByEmail(userUpdateDto.getEmail());
//            if (userWithNewEmail.isPresent()) {
//                throw new BadRequestException("The email '" + userUpdateDto.getEmail() + "' is already taken.");
//            }
//            currentUser.setEmail(userUpdateDto.getEmail());
//            tokenInvalidated = true; // Email change invalidates old token
//        }
//
//        User savedUser = userRepository.save(currentUser);
//        return new UserUpdateResult(savedUser, tokenInvalidated);
//    }
//}