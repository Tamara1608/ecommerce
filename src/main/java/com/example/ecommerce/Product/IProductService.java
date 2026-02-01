package com.example.ecommerce.Product;

import org.springframework.lang.NonNull;

import com.example.ecommerce.Product.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Product operations.
 */
public interface IProductService {
    
    @NonNull
    Product create(@NonNull Product product);
    
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

