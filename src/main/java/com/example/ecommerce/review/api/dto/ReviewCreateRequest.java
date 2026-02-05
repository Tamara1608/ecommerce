package com.example.ecommerce.review.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateRequest {
    private String comment;
    private Integer grade; // 1-5
    private Long userId;
    private Long productId;
}

