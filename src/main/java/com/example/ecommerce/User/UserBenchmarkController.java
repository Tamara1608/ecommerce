package com.example.ecommerce.User;

import com.example.ecommerce.User.LoginRequest;
import com.example.ecommerce.User.SignupRequest;
import com.example.ecommerce.User.entity.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for benchmarking cached vs non-cached user operations.
 * 
 * Routes:
 * - /api/benchmark/db/users/*      - Direct database access (no cache)
 * - /api/benchmark/cached/users/*  - Redis cached access
 */
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
    
    // ===========================================
    // DATABASE ROUTES (No Cache) - CRUD
    // ===========================================
    
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
    
    // ===========================================
    // DATABASE ROUTES (No Cache) - Auth
    // ===========================================
    
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
    
    // ===========================================
    // CACHED ROUTES (Redis Cache) - CRUD
    // ===========================================
    
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
    
    // ===========================================
    // CACHED ROUTES (Redis Cache) - Auth
    // ===========================================
    
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

