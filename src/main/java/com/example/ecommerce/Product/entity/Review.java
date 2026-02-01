package com.example.ecommerce.Product.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.example.ecommerce.User.entity.User;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    @ManyToOne
    private User user;

    private Integer rating;
    private String comment;

    private LocalDateTime createdAt;
}

