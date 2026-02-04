package com.example.ecommerce.user.app;

import com.example.ecommerce.user.api.dto.LoginRequest;
import com.example.ecommerce.user.api.dto.SignupRequest;
import com.example.ecommerce.user.domain.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUserService {
    
    @NonNull
    User create(@NonNull String username, @NonNull String email, @NonNull String password);
    
    @NonNull
    List<User> findAll();
    
    @NonNull
    User findById(@NonNull Long id);
    
    @NonNull
    User update(@NonNull Long id, @NonNull Map<String, Object> updates);
    
    void delete(@NonNull Long id);
    
    @NonNull
    ResponseEntity<User> signup(@NonNull SignupRequest request, @NonNull HttpSession session);
    
    @NonNull
    ResponseEntity<?> login(@NonNull LoginRequest request, @NonNull HttpSession session);
    
    @NonNull
    ResponseEntity<?> logout(@NonNull HttpSession session);
    
    @NonNull
    Optional<Long> getCurrentUserId(@NonNull HttpSession session);
}

