package com.example.ecommerce.product.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for updating an existing product.
 * All fields are optional to support partial updates (PATCH semantics).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequest {
    private String name;
    private String description;
    private Double price;
    private Integer discount;
    private String imageLink;
    
    private Set<Long> categoryIds;
}
