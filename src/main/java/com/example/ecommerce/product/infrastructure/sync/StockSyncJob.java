package com.example.ecommerce.product.infrastructure.sync;

import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;
import com.example.ecommerce.product.infrastructure.persistence.stock.StockTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockSyncJob {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductTable productTable;
    private final StockTable stockTable;
    
    private static final String STOCK_KEY_PREFIX = "stock:";
    
    @Scheduled(fixedRate = 120000) // Every 2 minutes
    @Transactional
    public void syncStockToDatabase() {
        log.info("Starting stock sync from Redis to Database...");
        
        Set<String> stockKeys = redisTemplate.keys(STOCK_KEY_PREFIX + "*");
        
        if (stockKeys == null || stockKeys.isEmpty()) {
            log.info("No stock keys found in Redis. Sync complete.");
            return;
        }
        
        int syncedCount = 0;
        int errorCount = 0;
        
        for (String key : stockKeys) {
            try {
                Long productId = extractProductId(key);
                if (productId == null) continue;
                
                Object stockValue = redisTemplate.opsForValue().get(key);
                if (stockValue == null) continue;
                
                Integer currentStock = ((Number) stockValue).intValue();
                
                Product product = productTable.findById(productId).orElse(null);
                if (product == null || product.getStock() == null) {
                    log.warn("Product {} or its stock not found, skipping", productId);
                    continue;
                }
                
                Long stockId = product.getStock().getId();
                stockTable.updateCurrentValue(stockId, currentStock);
                syncedCount++;
                
            } catch (Exception e) {
                log.error("Error syncing stock for key {}: {}", key, e.getMessage());
                errorCount++;
            }
        }
        
        log.info("Stock sync complete. Synced: {}, Errors: {}", syncedCount, errorCount);
    }
    
    private Long extractProductId(String key) {
        try {
            return Long.parseLong(key.replace(STOCK_KEY_PREFIX, ""));
        } catch (NumberFormatException e) {
            log.warn("Invalid stock key format: {}", key);
            return null;
        }
    }
}

