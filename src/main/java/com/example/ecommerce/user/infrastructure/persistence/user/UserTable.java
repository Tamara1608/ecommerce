package com.example.ecommerce.user.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.user.domain.User;

import java.util.Optional;

public interface UserTable extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

