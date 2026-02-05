package com.example.ecommerce.review.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;
    private String comment;
    private Integer grade;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private LocalDateTime createdAt;
}

