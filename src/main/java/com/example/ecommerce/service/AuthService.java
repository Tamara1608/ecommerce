package com.example.ecommerce.service;

import com.example.ecommerce.DTO.LoginRequest;
import com.example.ecommerce.DTO.SignupRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String SESSION_USER_ID = "USER_ID";

    private final UserRepository userRepository;

    public ResponseEntity<User> signup(SignupRequest request, HttpSession session) {
        Optional<User> existing = userRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        User saved = userRepository.save(user);

        session.setAttribute(SESSION_USER_ID, saved.getId());
        return ResponseEntity.ok(saved);
    }

    public ResponseEntity<?> login(LoginRequest request, HttpSession session) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        session.setAttribute(SESSION_USER_ID, user.getId());
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    public Optional<Long> getCurrentUserId(HttpSession session) {
        Object uid = session.getAttribute(SESSION_USER_ID);
        if (uid == null) return Optional.empty();
        return Optional.of(((Number) uid).longValue());
    }
}


