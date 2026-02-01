package com.example.ecommerce.Coupon;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.Order.entity.Coupon;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for Coupon CRUD and application operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class CouponService implements ICouponService {
    
    private static final String COUPON_USAGE_PREFIX = "coupon:used:";
    
    private final ICouponRepository couponRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public CouponService(ICouponRepository couponRepository, RedisTemplate<String, Object> redisTemplate) {
        this.couponRepository = couponRepository;
        this.redisTemplate = redisTemplate;
    }
    
    // -------------------
    // CRUD operations
    // -------------------
    
    @Override
    @NonNull
    public Coupon create(@NonNull Coupon coupon) {
        return couponRepository.create(coupon);
    }
    
    @Override
    @NonNull
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }
    
    @Override
    @NonNull
    public Coupon findById(@NonNull Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coupon not found with id: " + id
                ));
    }
    
    @Override
    @NonNull
    public Optional<Coupon> findByCode(@NonNull String code) {
        return couponRepository.findByCode(code);
    }
    
    @Override
    @NonNull
    public Coupon update(@NonNull Coupon coupon) {
        if (coupon.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coupon ID is required for update");
        }
        couponRepository.findById(coupon.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Coupon not found with id: " + coupon.getId()
                ));
        return couponRepository.update(coupon);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        if (!couponRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Coupon not found with id: " + id);
        }
        couponRepository.delete(id);
    }
    
    // -------------------
    // Coupon operations
    // -------------------
    
    @Override
    public boolean applyCoupon(@NonNull String code, @NonNull Long userId) {
        String usageKey = COUPON_USAGE_PREFIX + code;
        
        // Try to mark coupon as used by this user (atomic operation)
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(usageKey, userId, Duration.ofDays(1));
        
        if (Boolean.FALSE.equals(success)) {
            // Coupon already used
            return false;
        }
        
        // Get coupon and validate
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        Coupon coupon = couponOpt.orElse(null);
        
        if (!validateCoupon(coupon)) {
            // Validation failed - rollback the usage marker
            redisTemplate.delete(usageKey);
            return false;
        }
        
        // Mark coupon as used in database (async)
        CompletableFuture.runAsync(() -> {
            coupon.setUsed(true);
            couponRepository.update(coupon);
        });
        
        return true;
    }
    
    @Override
    public boolean validateCoupon(Coupon coupon) {
        return coupon != null
                && !coupon.getUsed()
                && coupon.getValidUntil() != null
                && !coupon.getValidUntil().isBefore(LocalDateTime.now());
    }
}

