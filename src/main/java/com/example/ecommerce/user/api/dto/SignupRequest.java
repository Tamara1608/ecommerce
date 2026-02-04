package com.example.ecommerce.user.api.dto;

import lombok.Data;

@Data
public class SignupRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
}

