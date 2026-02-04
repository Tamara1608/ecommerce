package com.example.ecommerce.user.api;

import com.example.ecommerce.user.app.UserService;
import com.example.ecommerce.user.api.dto.LoginRequest;
import com.example.ecommerce.user.api.dto.SignupRequest;
import com.example.ecommerce.user.domain.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/benchmark")
public class UserBenchmarkController {
    
    private final UserService dbUserService;
    private final UserService cachedUserService;
    
    public UserBenchmarkController(
            @Qualifier("dbUserService") UserService dbUserService,
            @Qualifier("cachedUserService") UserService cachedUserService) {
        this.dbUserService = dbUserService;
        this.cachedUserService = cachedUserService;
    }
    
    @GetMapping("/db/users")
    public List<User> dbFindAll() {
        return dbUserService.findAll();
    }
    
    @GetMapping("/db/users/{id}")
    public User dbFindById(@PathVariable Long id) {
        return dbUserService.findById(id);
    }
    
    @PostMapping("/db/users")
    public ResponseEntity<User> dbCreate(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        User created = dbUserService.create(username, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PatchMapping("/db/users/{id}")
    public User dbUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return dbUserService.update(id, updates);
    }
    
    @DeleteMapping("/db/users/{id}")
    public ResponseEntity<Void> dbDelete(@PathVariable Long id) {
        dbUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    
    @PostMapping("/db/auth/signup")
    public ResponseEntity<User> dbSignup(@RequestBody SignupRequest request, HttpSession session) {
        return dbUserService.signup(request, session);
    }
    
    @PostMapping("/db/auth/login")
    public ResponseEntity<?> dbLogin(@RequestBody LoginRequest request, HttpSession session) {
        return dbUserService.login(request, session);
    }
    
    @PostMapping("/db/auth/logout")
    public ResponseEntity<?> dbLogout(HttpSession session) {
        return dbUserService.logout(session);
    }
    
    @GetMapping("/db/auth/me")
    public ResponseEntity<?> dbMe(HttpSession session) {
        return dbUserService.getCurrentUserId(session)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in"));
    }
    
    
    @GetMapping("/cached/users")
    public List<User> cachedFindAll() {
        return cachedUserService.findAll();
    }
    
    @GetMapping("/cached/users/{id}")
    public User cachedFindById(@PathVariable Long id) {
        return cachedUserService.findById(id);
    }
    
    @PostMapping("/cached/users")
    public ResponseEntity<User> cachedCreate(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {
        User created = cachedUserService.create(username, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PatchMapping("/cached/users/{id}")
    public User cachedUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return cachedUserService.update(id, updates);
    }
    
    @DeleteMapping("/cached/users/{id}")
    public ResponseEntity<Void> cachedDelete(@PathVariable Long id) {
        cachedUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    
    @PostMapping("/cached/auth/signup")
    public ResponseEntity<User> cachedSignup(@RequestBody SignupRequest request, HttpSession session) {
        return cachedUserService.signup(request, session);
    }
    
    @PostMapping("/cached/auth/login")
    public ResponseEntity<?> cachedLogin(@RequestBody LoginRequest request, HttpSession session) {
        return cachedUserService.login(request, session);
    }
    
    @PostMapping("/cached/auth/logout")
    public ResponseEntity<?> cachedLogout(HttpSession session) {
        return cachedUserService.logout(session);
    }
    
    @GetMapping("/cached/auth/me")
    public ResponseEntity<?> cachedMe(HttpSession session) {
        return cachedUserService.getCurrentUserId(session)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in"));
    }
}

