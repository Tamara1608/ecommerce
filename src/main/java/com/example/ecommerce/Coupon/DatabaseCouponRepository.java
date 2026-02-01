package com.example.ecommerce.Coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.Order.entity.Coupon;

import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for Coupon CRUD operations.
 * Uses CouponTable (JPA repository) to perform database operations.
 * Direct database access without caching.
 */
@Repository
@Qualifier("dbCouponRepository")
@RequiredArgsConstructor
public class DatabaseCouponRepository implements ICouponRepository {
    
    private final CouponTable couponTable;
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public Coupon create(@NonNull Coupon coupon) {
        return couponTable.save(coupon);
    }
    
    // -------------------
    // READ operations
    // -------------------
    
    @Override
    @NonNull
    public List<Coupon> findAll() {
        return couponTable.findAll();
    }
    
    @Override
    @NonNull
    public Optional<Coupon> findById(@NonNull Long id) {
        return couponTable.findById(id);
    }
    
    @Override
    @NonNull
    public Optional<Coupon> findByCode(@NonNull String code) {
        return couponTable.findByCode(code);
    }
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    @Override
    @NonNull
    public Coupon update(@NonNull Coupon coupon) {
        return couponTable.save(coupon);
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        couponTable.deleteById(id);
    }
    
    @Override
    public boolean existsById(@NonNull Long id) {
        return couponTable.existsById(id);
    }
}

