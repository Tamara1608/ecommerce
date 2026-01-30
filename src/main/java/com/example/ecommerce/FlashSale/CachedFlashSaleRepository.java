package com.example.ecommerce.FlashSale;

import com.example.ecommerce.entity.FlashSaleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cached repository implementation for FlashSaleEvent CRUD operations.
 * Uses optimistic caching strategy - checks cache first, falls back to database.
 * Caches entire FlashSaleEvent objects with key pattern: flashsale:{id}
 * All flash sales are preloaded into cache by a cron job.
 */
@Repository
@Qualifier("cachedFlashSaleRepository")
@RequiredArgsConstructor
public class CachedFlashSaleRepository implements IFlashSaleRepository {
    
    private final FlashSaleTable flashSaleTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "flashsale:";
    private static final String ALL_FLASH_SALES_KEY = "flashsales:all";
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public FlashSaleEvent create(@NonNull FlashSaleEvent flashSale) {
        FlashSaleEvent saved = flashSaleTable.save(flashSale);
        cacheFlashSale(saved);
        invalidateAllFlashSalesCache();
        return saved;
    }
    
    // -------------------
    // READ operations
    // -------------------
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<FlashSaleEvent> findAll() {
        List<FlashSaleEvent> cached = (List<FlashSaleEvent>) redisTemplate.opsForValue().get(ALL_FLASH_SALES_KEY);
        return cached != null ? cached : new ArrayList<>();
    }
    
    @Override
    @NonNull
    public Optional<FlashSaleEvent> findById(@NonNull Long id) {
        // Check cache first (optimistic caching)
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((FlashSaleEvent) cached);
        }
        
        // Cache miss - fetch from database
        Optional<FlashSaleEvent> flashSaleOpt = flashSaleTable.findById(id);
        
        // Cache the result if found
        flashSaleOpt.ifPresent(this::cacheFlashSale);
        
        return flashSaleOpt;
    }
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    @Override
    @NonNull
    public FlashSaleEvent update(@NonNull FlashSaleEvent flashSale) {
        FlashSaleEvent updated = flashSaleTable.save(flashSale);
        cacheFlashSale(updated);
        invalidateAllFlashSalesCache();
        return updated;
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        flashSaleTable.deleteById(id);
        evictFromCache(id);
        invalidateAllFlashSalesCache();
    }
    
    // -------------------
    // Cache helper methods
    // -------------------
    
    private void cacheFlashSale(FlashSaleEvent flashSale) {
        String cacheKey = CACHE_KEY_PREFIX + flashSale.getId();
        redisTemplate.opsForValue().set(cacheKey, flashSale);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void invalidateAllFlashSalesCache() {
        redisTemplate.delete(ALL_FLASH_SALES_KEY);
    }
}

