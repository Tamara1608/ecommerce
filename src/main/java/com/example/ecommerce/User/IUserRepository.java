package com.example.ecommerce.User;

import org.springframework.lang.NonNull;

import com.example.ecommerce.User.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User CRUD operations.
 * All methods are stateful and result in permanent changes.
 */
public interface IUserRepository {
    
    // -------------------
    // CREATE operations
    // -------------------
    
    /**
     * Create a new user entity.
     * Stateful operation - persists immediately.
     * 
     * @param user the user to create
     * @return the created user with generated ID
     */
    @NonNull
    User create(@NonNull User user);
    
    // -------------------
    // READ operations
    // -------------------
    
    /**
     * Find all users.
     * 
     * @return list of all users
     */
    @NonNull
    List<User> findAll();
    
    /**
     * Find a user by ID.
     * 
     * @param id the user ID
     * @return Optional containing the user if found, empty otherwise
     */
    @NonNull
    Optional<User> findById(@NonNull Long id);
    
    /**
     * Find a user by username.
     * 
     * @param username the username
     * @return Optional containing the user if found, empty otherwise
     */
    @NonNull
    Optional<User> findByUsername(@NonNull String username);
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    /**
     * Update an existing user entity.
     * Stateful operation - persists immediately.
     * 
     * @param user the user to update
     * @return the updated user
     */
    @NonNull
    User update(@NonNull User user);
    
    // -------------------
    // DELETE operations
    // -------------------
    
    /**
     * Delete a user by ID.
     * Stateful operation - deletes immediately.
     * 
     * @param id the user ID to delete
     */
    void delete(@NonNull Long id);
    
    /**
     * Check if a user exists by ID.
     * 
     * @param id the user ID
     * @return true if user exists, false otherwise
     */
    boolean existsById(@NonNull Long id);
}

