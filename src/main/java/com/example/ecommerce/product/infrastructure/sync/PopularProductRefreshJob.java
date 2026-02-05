package com.example.ecommerce.product.infrastructure.sync;

import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularProductRefreshJob {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductTable productTable;
    
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final String POPULAR_PRODUCTS_KEY = "analytics:popular-products";
    private static final long PRODUCT_CACHE_TTL_MINUTES = 10;
    private static final int TOP_N_PRODUCTS = 20;
    
    @Scheduled(fixedRate = 120000) // Every 2 minutes
    public void refreshPopularProducts() {
        log.debug("Starting refresh-ahead for popular products...");
        
        Set<ZSetOperations.TypedTuple<Object>> popularProducts = redisTemplate.opsForZSet()
            .reverseRangeWithScores(POPULAR_PRODUCTS_KEY, 0, TOP_N_PRODUCTS - 1);
        
        if (popularProducts == null || popularProducts.isEmpty()) {
            log.debug("No popular products tracked yet");
            return;
        }
        
        int refreshedCount = 0;
        
        for (ZSetOperations.TypedTuple<Object> tuple : popularProducts) {
            if (tuple.getValue() == null) continue;
            
            try {
                Long productId = Long.parseLong(tuple.getValue().toString());
                String cacheKey = CACHE_KEY_PREFIX + productId;
                
                Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.MINUTES);
                
                if (ttl == null || ttl <= 2) {
                    Optional<Product> productOpt = productTable.findById(productId);
                    
                    if (productOpt.isPresent()) {
                        redisTemplate.opsForValue().set(
                            cacheKey, 
                            productOpt.get(), 
                            PRODUCT_CACHE_TTL_MINUTES, 
                            TimeUnit.MINUTES
                        );
                        refreshedCount++;
                        log.debug("Refreshed popular product {} (views: {})", 
                            productId, tuple.getScore());
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid product ID in popular products: {}", tuple.getValue());
            }
        }
        
        if (refreshedCount > 0) {
            log.info("Refresh-ahead for popular products complete. Refreshed: {}", refreshedCount);
        }
    }
    
    public void trackProductView(Long productId) {
        redisTemplate.opsForZSet().incrementScore(
            POPULAR_PRODUCTS_KEY, 
            productId.toString(), 
            1.0
        );
    }
}

