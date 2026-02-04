package com.example.ecommerce.coupon.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.coupon.domain.Coupon;

import java.util.Optional;

public interface CouponTable extends JpaRepository<Coupon, Long> {
    
    Optional<Coupon> findByCode(String code);
}

