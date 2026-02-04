package com.example.ecommerce.flashsale.infrastructure.cache.flashsale;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.flashsale.domain.FlashSaleEvent;
import com.example.ecommerce.flashsale.infrastructure.persistence.flashsale.IFlashSaleRepository;
import com.example.ecommerce.flashsale.infrastructure.persistence.flashsale.FlashSaleTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("cachedFlashSaleRepository")
@RequiredArgsConstructor
public class CachedFlashSaleRepository implements IFlashSaleRepository {
    
    private final FlashSaleTable flashSaleTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "flashsale:";
    private static final String ALL_FLASH_SALES_KEY = "flashsales:all";
    
    @Override
    @NonNull
    public FlashSaleEvent create(@NonNull FlashSaleEvent flashSale) {
        FlashSaleEvent saved = flashSaleTable.save(flashSale);
        cacheFlashSale(saved);
        invalidateAllFlashSalesCache();
        return saved;
    }
    
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
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((FlashSaleEvent) cached);
        }
        
        Optional<FlashSaleEvent> flashSaleOpt = flashSaleTable.findById(id);
        
        flashSaleOpt.ifPresent(this::cacheFlashSale);
        
        return flashSaleOpt;
    }
    
    @Override
    @NonNull
    public FlashSaleEvent update(@NonNull FlashSaleEvent flashSale) {
        FlashSaleEvent updated = flashSaleTable.save(flashSale);
        cacheFlashSale(updated);
        invalidateAllFlashSalesCache();
        return updated;
    }
    
    @Override
    public void delete(@NonNull Long id) {
        flashSaleTable.deleteById(id);
        evictFromCache(id);
        invalidateAllFlashSalesCache();
    }
    
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

