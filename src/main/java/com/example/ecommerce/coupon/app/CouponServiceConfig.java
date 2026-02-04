package com.example.ecommerce.coupon.app;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.example.ecommerce.coupon.infrastructure.cache.ICouponRepository;

/**
 * Configuration for CouponService beans.
 * Creates two service instances - one with caching, one without.
 */
@Configuration
public class CouponServiceConfig {
    
    @Bean
    @Qualifier("dbCouponService")
    public CouponService dbCouponService(
            @Qualifier("dbCouponRepository") ICouponRepository repository,
            RedisTemplate<String, Object> redisTemplate) {
        return new CouponService(repository, redisTemplate);
    }
    
    @Bean
    @Qualifier("cachedCouponService")
    public CouponService cachedCouponService(
            @Qualifier("cachedCouponRepository") ICouponRepository repository,
            RedisTemplate<String, Object> redisTemplate) {
        return new CouponService(repository, redisTemplate);
    }
}

