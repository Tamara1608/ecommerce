package com.example.ecommerce.category.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new category.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateRequest {
    private String name;
}

