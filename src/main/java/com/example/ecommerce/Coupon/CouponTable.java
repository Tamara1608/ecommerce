package com.example.ecommerce.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.Order.entity.Coupon;

import java.util.Optional;

/**
 * JPA repository for Coupon entity.
 * Provides direct database access methods.
 */
public interface CouponTable extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
}

