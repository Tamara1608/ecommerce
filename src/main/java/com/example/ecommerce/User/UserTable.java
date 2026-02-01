package com.example.ecommerce.User;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.User.entity.User;

import java.util.Optional;

/**
 * JPA repository for User entity.
 * Provides direct database access methods.
 */
public interface UserTable extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
}

