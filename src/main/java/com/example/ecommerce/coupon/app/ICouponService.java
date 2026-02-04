package com.example.ecommerce.coupon.app;

import org.springframework.lang.NonNull;

import com.example.ecommerce.coupon.domain.Coupon;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Coupon operations.
 * Includes CRUD and coupon application methods.
 */
public interface ICouponService {
    
    // -------------------
    // CRUD operations
    // -------------------
    
    @NonNull
    Coupon create(@NonNull Coupon coupon);
    
    @NonNull
    List<Coupon> findAll();
    
    @NonNull
    Coupon findById(@NonNull Long id);
    
    @NonNull
    Optional<Coupon> findByCode(@NonNull String code);
    
    @NonNull
    Coupon update(@NonNull Coupon coupon);
    
    void delete(@NonNull Long id);
    
    // -------------------
    // Coupon operations
    // -------------------
    
    /**
     * Apply a coupon for a user.
     * Validates the coupon and marks it as used if valid.
     * 
     * @param code the coupon code
     * @param userId the user ID applying the coupon
     * @return true if coupon was successfully applied, false otherwise
     */
    boolean applyCoupon(@NonNull String code, @NonNull Long userId);
    
    /**
     * Validate a coupon.
     * Checks if coupon exists, is not used, and is not expired.
     * 
     * @param coupon the coupon to validate
     * @return true if valid, false otherwise
     */
    boolean validateCoupon(Coupon coupon);
}

