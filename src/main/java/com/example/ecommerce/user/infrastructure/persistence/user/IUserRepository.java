package com.example.ecommerce.user.infrastructure.persistence.user;

import org.springframework.lang.NonNull;

import com.example.ecommerce.user.domain.User;

import java.util.List;
import java.util.Optional;


public interface IUserRepository {
    
    
    @NonNull
    User create(@NonNull User user);
    
    @NonNull
    List<User> findAll();

    @NonNull
    Optional<User> findById(@NonNull Long id);

    @NonNull
    Optional<User> findByUsername(@NonNull String username);

    boolean existsById(@NonNull Long id);

    @NonNull
    User update(@NonNull User user);

    void delete(@NonNull Long id);
    
}

