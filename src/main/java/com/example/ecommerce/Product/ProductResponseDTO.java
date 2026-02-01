package com.example.ecommerce.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for retrieving product data with stock information.
 * Used for displaying products to users.
 * 
 * For order creation, use ProductDTO instead (without stock fields).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer discount;
    private String imageLink;
    private Integer currentStock;  // Current stock from Redis cache
}

