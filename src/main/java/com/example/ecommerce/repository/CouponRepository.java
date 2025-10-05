package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, Serializable {
    Optional<Coupon> findByCode(String code);

}
