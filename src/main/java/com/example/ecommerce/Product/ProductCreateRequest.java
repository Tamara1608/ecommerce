package com.example.ecommerce.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for creating a new product.
 * Includes stock quantity and category IDs.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateRequest {
    private String name;
    private String description;
    private Double price;
    private Integer discount;
    private String imageLink;
    
    // Stock quantity to initialize
    private Integer stockQuantity;
    
    // Category IDs to associate with the product
    private Set<Long> categoryIds;
}

