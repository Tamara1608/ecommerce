package com.example.ecommerce.user.infrastructure.persistence.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.user.domain.User;

import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("dbUserRepository")
@RequiredArgsConstructor
public class DatabaseUserRepository implements IUserRepository {
    
    private final UserTable userTable;
    
    @Override
    @NonNull
    public User create(@NonNull User user) {
        return userTable.save(user);
    }
    
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
    
    @Override
    public boolean existsById(@NonNull Long id) {
        return userTable.existsById(id);
    }
    
    @Override
    @NonNull
    public User update(@NonNull User user) {
        return userTable.save(user);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        userTable.deleteById(id);
    }
    
}

