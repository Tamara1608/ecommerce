package com.example.ecommerce.User;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.User.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for User CRUD operations.
 * Uses UserTable (JPA repository) to perform database operations.
 * Direct database access without caching.
 */
@Repository
@Qualifier("dbUserRepository")
@RequiredArgsConstructor
public class DatabaseUserRepository implements IUserRepository {
    
    private final UserTable userTable;
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public User create(@NonNull User user) {
        return userTable.save(user);
    }
    
    // -------------------
    // READ operations
    // -------------------
    
    @Override
    @NonNull
    public List<User> findAll() {
        return userTable.findAll();
    }
    
    @Override
    @NonNull
    public Optional<User> findById(@NonNull Long id) {
        return userTable.findById(id);
    }
    
    @Override
    @NonNull
    public Optional<User> findByUsername(@NonNull String username) {
        return userTable.findByUsername(username);
    }
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    @Override
    @NonNull
    public User update(@NonNull User user) {
        return userTable.save(user);
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        userTable.deleteById(id);
    }
    
    @Override
    public boolean existsById(@NonNull Long id) {
        return userTable.existsById(id);
    }
}

