package com.example.ecommerce.controller;


import com.example.ecommerce.entity.User;
import com.example.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/user")
    public User createUser(@RequestParam String username, @RequestParam String email, @RequestParam String password) {
        return userService.createUser(username, email, password);
    }

    @GetMapping("/user/{id}")
    public User getUsers(@PathVariable Long id) {
        return userService.getUser(id);
    }
}
