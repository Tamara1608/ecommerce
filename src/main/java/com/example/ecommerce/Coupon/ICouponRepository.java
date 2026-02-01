package com.example.ecommerce.Coupon;

import org.springframework.lang.NonNull;

import com.example.ecommerce.Order.entity.Coupon;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Coupon CRUD operations.
 * All methods are stateful and result in permanent changes.
 */
public interface ICouponRepository {
    
    // -------------------
    // CREATE operations
    // -------------------
    
    /**
     * Create a new coupon entity.
     * Stateful operation - persists immediately.
     * 
     * @param coupon the coupon to create
     * @return the created coupon with generated ID
     */
    @NonNull
    Coupon create(@NonNull Coupon coupon);
    
    // -------------------
    // READ operations
    // -------------------
    
    /**
     * Find all coupons.
     * 
     * @return list of all coupons
     */
    @NonNull
    List<Coupon> findAll();
    
    /**
     * Find a coupon by ID.
     * 
     * @param id the coupon ID
     * @return Optional containing the coupon if found, empty otherwise
     */
    @NonNull
    Optional<Coupon> findById(@NonNull Long id);
    
    /**
     * Find a coupon by code.
     * 
     * @param code the coupon code
     * @return Optional containing the coupon if found, empty otherwise
     */
    @NonNull
    Optional<Coupon> findByCode(@NonNull String code);
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    /**
     * Update an existing coupon entity.
     * Stateful operation - persists immediately.
     * 
     * @param coupon the coupon to update
     * @return the updated coupon
     */
    @NonNull
    Coupon update(@NonNull Coupon coupon);
    
    // -------------------
    // DELETE operations
    // -------------------
    
    /**
     * Delete a coupon by ID.
     * Stateful operation - deletes immediately.
     * 
     * @param id the coupon ID to delete
     */
    void delete(@NonNull Long id);
    
    /**
     * Check if a coupon exists by ID.
     * 
     * @param id the coupon ID
     * @return true if coupon exists, false otherwise
     */
    boolean existsById(@NonNull Long id);
}

