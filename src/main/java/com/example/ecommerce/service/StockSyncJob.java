package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StockSyncJob {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductRepository productRepository;

    private static final String STOCK_KEY_PREFIX = "stock:";

    // Runs every 2 minutes
    @Scheduled(fixedRate = 120000)
    public void syncStockFromRedisToDb() {
        System.out.println("🔄 Sync job started...");

        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            String stockKey = STOCK_KEY_PREFIX + product.getId();
            Object redisStock = redisTemplate.opsForValue().get(stockKey);

            if (redisStock != null) {
                try {
                    int stockValue = Integer.parseInt(redisStock.toString());

                    if (product.getStock() != stockValue) {
                        product.setStock(stockValue);
                        productRepository.save(product);
                        System.out.println("✅ Synced product " + product.getId() + " stock: " + stockValue);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Invalid stock value in Redis for key: " + stockKey);
                }
            }
        }

        System.out.println("✔️ Sync job finished.");
    }
}