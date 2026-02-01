package com.example.ecommerce.User;

import com.example.ecommerce.User.LoginRequest;
import com.example.ecommerce.User.SignupRequest;
import com.example.ecommerce.User.entity.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for User operations.
 * Includes CRUD and authentication methods.
 */
public interface IUserService {
    
    // -------------------
    // CRUD operations
    // -------------------
    
    @NonNull
    User create(@NonNull String username, @NonNull String email, @NonNull String password);
    
    @NonNull
    List<User> findAll();
    
    @NonNull
    User findById(@NonNull Long id);
    
    @NonNull
    User update(@NonNull Long id, @NonNull Map<String, Object> updates);
    
    void delete(@NonNull Long id);
    
    // -------------------
    // Authentication operations
    // -------------------
    
    /**
     * Sign up a new user.
     * 
     * @param request the signup request
     * @param session the HTTP session
     * @return ResponseEntity with the created user or error
     */
    @NonNull
    ResponseEntity<User> signup(@NonNull SignupRequest request, @NonNull HttpSession session);
    
    /**
     * Log in a user.
     * 
     * @param request the login request
     * @param session the HTTP session
     * @return ResponseEntity with the user or error
     */
    @NonNull
    ResponseEntity<?> login(@NonNull LoginRequest request, @NonNull HttpSession session);
    
    /**
     * Log out the current user.
     * 
     * @param session the HTTP session
     * @return ResponseEntity indicating success
     */
    @NonNull
    ResponseEntity<?> logout(@NonNull HttpSession session);
    
    /**
     * Get the current logged in user's ID.
     * 
     * @param session the HTTP session
     * @return Optional containing the user ID if logged in
     */
    @NonNull
    Optional<Long> getCurrentUserId(@NonNull HttpSession session);
}

