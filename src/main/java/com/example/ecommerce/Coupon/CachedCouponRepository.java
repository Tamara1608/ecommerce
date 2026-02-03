package com.example.ecommerce.Coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.Order.entity.Coupon;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cached repository implementation for Coupon CRUD operations.
 * Uses optimistic caching strategy - checks cache first, falls back to database.
 * Caches entire Coupon objects with key pattern: coupon:{couponId}
 * Code lookups cached with key pattern: coupon:code:{code}
 * Coupon TTL is set based on validUntil date.
 */
@Repository
@Qualifier("cachedCouponRepository")
@RequiredArgsConstructor
public class CachedCouponRepository implements ICouponRepository {
    
    private final CouponTable couponTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "coupon:";
    private static final String CODE_KEY_PREFIX = "coupon:code:";
    private static final String ALL_COUPONS_KEY = "coupons:all";
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public Coupon create(@NonNull Coupon coupon) {
        Coupon saved = couponTable.save(coupon);
        cacheCoupon(saved);
        cacheByCode(saved);
        invalidateAllCouponsCache();
        return saved;
    }
    
    // -------------------
    // READ operations
    // -------------------
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Coupon> findAll() {
        List<Coupon> cached = (List<Coupon>) redisTemplate.opsForValue().get(ALL_COUPONS_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        // Cache miss - fetch from database
        List<Coupon> coupons = couponTable.findAll();
        
        // Cache all coupons
        cacheAllCoupons(coupons);
        
        return coupons;
    }
    
    @Override
    @NonNull
    public Optional<Coupon> findById(@NonNull Long id) {
        // Check cache first (optimistic caching)
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((Coupon) cached);
        }
        
        // Cache miss - fetch from database
        Optional<Coupon> couponOpt = couponTable.findById(id);
        
        // Cache the result if found
        couponOpt.ifPresent(coupon -> {
            cacheCoupon(coupon);
            cacheByCode(coupon);
        });
        
        return couponOpt;
    }
    
    @Override
    @NonNull
    public Optional<Coupon> findByCode(@NonNull String code) {
        // Check cache first
        String cacheKey = CODE_KEY_PREFIX + code;
        Object cachedId = redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedId != null) {
            // Get coupon by cached ID
            Long couponId = ((Number) cachedId).longValue();
            return findById(couponId);
        }
        
        // Cache miss - fetch from database
        Optional<Coupon> couponOpt = couponTable.findByCode(code);
        
        // Cache the result if found
        couponOpt.ifPresent(coupon -> {
            cacheCoupon(coupon);
            cacheByCode(coupon);
        });
        
        return couponOpt;
    }
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    @Override
    @NonNull
    public Coupon update(@NonNull Coupon coupon) {
        // Get old coupon to invalidate old code cache if changed
        Optional<Coupon> oldCouponOpt = couponTable.findById(coupon.getId());
        oldCouponOpt.ifPresent(oldCoupon -> {
            if (!oldCoupon.getCode().equals(coupon.getCode())) {
                evictCodeCache(oldCoupon.getCode());
            }
        });
        
        Coupon updated = couponTable.save(coupon);
        cacheCoupon(updated);
        cacheByCode(updated);
        invalidateAllCouponsCache();
        return updated;
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        // Get coupon to invalidate code cache
        Optional<Coupon> couponOpt = couponTable.findById(id);
        couponOpt.ifPresent(coupon -> evictCodeCache(coupon.getCode()));
        
        couponTable.deleteById(id);
        evictFromCache(id);
        invalidateAllCouponsCache();
    }
    
    @Override
    public boolean existsById(@NonNull Long id) {
        // Check cache first
        String cacheKey = CACHE_KEY_PREFIX + id;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            return true;
        }
        return couponTable.existsById(id);
    }
    
    // -------------------
    // Cache helper methods
    // -------------------
    
    private void cacheCoupon(Coupon coupon) {
        String cacheKey = CACHE_KEY_PREFIX + coupon.getId();
        Duration ttl = calculateTtl(coupon);
        
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(cacheKey, coupon, ttl);
        } else {
            redisTemplate.opsForValue().set(cacheKey, coupon);
        }
    }
    
    private void cacheByCode(Coupon coupon) {
        String cacheKey = CODE_KEY_PREFIX + coupon.getCode();
        Duration ttl = calculateTtl(coupon);
        
        if (!ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(cacheKey, coupon.getId(), ttl);
        } else {
            redisTemplate.opsForValue().set(cacheKey, coupon.getId());
        }
    }
    
    private Duration calculateTtl(Coupon coupon) {
        if (coupon.getValidUntil() == null) {
            return Duration.ZERO; // No TTL
        }
        return Duration.between(LocalDateTime.now(), coupon.getValidUntil());
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void evictCodeCache(String code) {
        String cacheKey = CODE_KEY_PREFIX + code;
        redisTemplate.delete(cacheKey);
    }
    
    private void cacheAllCoupons(List<Coupon> coupons) {
        redisTemplate.opsForValue().set(ALL_COUPONS_KEY, coupons);
        // Also cache individual coupons
        coupons.forEach(coupon -> {
            cacheCoupon(coupon);
            cacheByCode(coupon);
        });
    }
    
    private void invalidateAllCouponsCache() {
        redisTemplate.delete(ALL_COUPONS_KEY);
    }
}

