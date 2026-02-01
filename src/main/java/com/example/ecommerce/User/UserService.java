package com.example.ecommerce.User;

import com.example.ecommerce.User.LoginRequest;
import com.example.ecommerce.User.SignupRequest;
import com.example.ecommerce.User.entity.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for User CRUD and authentication operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class UserService implements IUserService {
    
    public static final String SESSION_USER_ID = "USER_ID";
    
    private final IUserRepository userRepository;
    
    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // -------------------
    // CRUD operations
    // -------------------
    
    @Override
    @NonNull
    public User create(@NonNull String username, @NonNull String email, @NonNull String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return userRepository.create(user);
    }
    
    @Override
    @NonNull
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    @Override
    @NonNull
    public User findById(@NonNull Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id
                ));
    }
    
    @Override
    @NonNull
    public User update(@NonNull Long id, @NonNull Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + id
                ));
        
        if (updates.containsKey("username")) {
            user.setUsername((String) updates.get("username"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("password")) {
            user.setPassword((String) updates.get("password"));
        }
        if (updates.containsKey("firstName")) {
            user.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            user.setLastName((String) updates.get("lastName"));
        }
        
        return userRepository.update(user);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
        }
        userRepository.delete(id);
    }
    
    // -------------------
    // Authentication operations
    // -------------------
    
    @Override
    @NonNull
    public ResponseEntity<User> signup(@NonNull SignupRequest request, @NonNull HttpSession session) {
        Optional<User> existing = userRepository.findByUsername(request.getUsername());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        User saved = userRepository.create(user);
        
        session.setAttribute(SESSION_USER_ID, saved.getId());
        return ResponseEntity.ok(saved);
    }
    
    @Override
    @NonNull
    public ResponseEntity<?> login(@NonNull LoginRequest request, @NonNull HttpSession session) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        session.setAttribute(SESSION_USER_ID, user.getId());
        return ResponseEntity.ok(user);
    }
    
    @Override
    @NonNull
    public ResponseEntity<?> logout(@NonNull HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }
    
    @Override
    @NonNull
    public Optional<Long> getCurrentUserId(@NonNull HttpSession session) {
        Object uid = session.getAttribute(SESSION_USER_ID);
        if (uid == null) return Optional.empty();
        return Optional.of(((Number) uid).longValue());
    }
}

