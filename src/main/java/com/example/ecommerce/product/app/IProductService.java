package com.example.ecommerce.product.app;

import org.springframework.lang.NonNull;

import com.example.ecommerce.product.api.dto.ProductCreateRequest;
import com.example.ecommerce.product.domain.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Product operations.
 */
public interface IProductService {
    
    @NonNull
    Product create(@NonNull Product product);
    
    /**
     * Create a product with stock, price history, and categories.
     */
    @NonNull
    Product createWithDetails(@NonNull ProductCreateRequest request);
    
    @NonNull
    List<Product> findAll();
    
    @NonNull
    Product findById(@NonNull Long id);
    
    @NonNull
    Product update(@NonNull Product product);
    
    void delete(@NonNull Long id);
    
    /**
     * Check stock and return product if in stock, otherwise return empty.
     * Decrements stock atomically in Redis.
     * 
     * @param productId the product ID
     * @param quantity the quantity to reserve
     * @return Optional containing the product if in stock, empty if sold out
     */
    @NonNull
    Optional<Product> returnIfInStock(@NonNull Long productId, int quantity);
}

