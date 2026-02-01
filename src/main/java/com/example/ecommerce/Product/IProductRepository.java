package com.example.ecommerce.Product;

import org.springframework.lang.NonNull;

import com.example.ecommerce.Product.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product CRUD operations.
 * All methods are stateful and result in permanent changes.
 */
public interface IProductRepository {
    
    // -------------------
    // CREATE operations
    // -------------------
    
    /**
     * Create a new product entity.
     * Stateful operation - persists immediately.
     * 
     * @param product the product to create
     * @return the created product with generated ID
     */
    @NonNull
    Product create(@NonNull Product product);
    
    // -------------------
    // READ operations
    // -------------------
    
    /**
     * Find all products.
     * 
     * @return list of all products
     */
    @NonNull
    List<Product> findAll();
    
    /**
     * Find a product by ID.
     * 
     * @param id the product ID
     * @return Optional containing the product if found, empty otherwise
     */
    @NonNull
    Optional<Product> findById(@NonNull Long id);
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    /**
     * Update an existing product entity.
     * Stateful operation - persists immediately.
     * 
     * @param product the product to update
     * @return the updated product
     */
    @NonNull
    Product update(@NonNull Product product);
    
    // -------------------
    // DELETE operations
    // -------------------
    
    /**
     * Delete a product by ID.
     * Stateful operation - deletes immediately.
     * 
     * @param id the product ID to delete
     */
    void delete(@NonNull Long id);
    
    // -------------------
    // STOCK operations
    // -------------------
    
    /**
     * Check if product has sufficient stock and decrement if available.
     * Returns the product if stock is available, empty otherwise.
     * 
     * @param productId the product ID
     * @param quantity the quantity to check/decrement
     * @return Optional containing the product if in stock, empty otherwise
     */
    @NonNull
    Optional<Product> returnIfInStock(@NonNull Long productId, int quantity);
}
