package com.example.ecommerce.service;

import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String COUPON_METADATA_PREFIX = "coupon:data:";
    private final String COUPON_USAGE_PREFIX = "coupon:used:";

    public boolean applyCoupon(String code, Long userId) {
        String usageKey = COUPON_USAGE_PREFIX + code;

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(usageKey, userId, Duration.ofDays(1)); // think about this part

        if (Boolean.FALSE.equals(success)) {
            return false;
        }

        Coupon coupon = getCouponMetadata(code).orElse(null);

        if(!validateCoupon(coupon)){
            deleteCoupon(usageKey);
            return false;
        }

        CompletableFuture.runAsync(() -> {
            coupon.setUsed(true);
            couponRepository.save(coupon);
        });

        return true;

    }

    public List<Coupon> getAllCoupons(){
        return couponRepository.findAll();
    }

    public void deleteCoupon(String key) {
        redisTemplate.delete(key);
    }

    public Boolean validateCoupon(Coupon coupon) {

        return coupon != null
                && !coupon.getUsed()
                && !coupon.getValidUntil().isBefore(LocalDateTime.now());
    }

    public Optional<Coupon> getCouponMetadata(String code) {
        String key = COUPON_METADATA_PREFIX + code;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached instanceof Coupon c) {
            return Optional.of(c);
        }

        return couponRepository.findByCode(code).map(c -> {
            cacheCoupon(c);
            return c;
        });
    }
    public void cacheCoupon(Coupon coupon) {
        String key = COUPON_METADATA_PREFIX + coupon.getCode();
        long secondsUntilExpiry = Duration
                .between(java.time.LocalDateTime.now(), coupon.getValidUntil())
                .getSeconds();

        if (secondsUntilExpiry > 0) {
            redisTemplate.opsForValue().set(key, coupon, Duration.ofSeconds(secondsUntilExpiry));
        }
    }

}

