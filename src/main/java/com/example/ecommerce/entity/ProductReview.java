package com.example.ecommerce.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ProductReview { // agregated table that can be cached?
    @Id
    private Long productId;

    private Double averageRating;
    private Integer reviewCount;
}
