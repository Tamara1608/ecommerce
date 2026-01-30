/*
package com.example.ecommerce.controller;

import com.example.ecommerce.DTO.LoginRequest;
import com.example.ecommerce.DTO.SignupRequest;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody SignupRequest request, HttpSession session) {
        return authService.signup(request, session);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request, session);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        return authService.logout(session);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        return authService.getCurrentUserId(session)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in"));
    }
}
*/
