package com.example.ecommerce.generator;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RedisPreloader implements CommandLineRunner {

    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String PRODUCT_KEY_PREFIX = "products::";
    private final String STOCK_KEY_PREFIX = "stock:";

    public RedisPreloader(ProductService productService, RedisTemplate<String, Object> redisTemplate) {
        this.productService = productService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        Iterable<Product> products = productService.getAllProducts();

        for (Product p : products) {
            redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + p.getId(),
                    new Product(p.getId(), p.getName(), p.getDescription(), p.getPrice(), null)); // stock = null

            // Store stock separately
            // Only set stock if it does not exist in Redis
            String stockKey = STOCK_KEY_PREFIX + p.getId();
            if (redisTemplate.opsForValue().get(stockKey) == null) {
                redisTemplate.opsForValue().set(stockKey, p.getStock());
            }
        }

        System.out.println("✅ Redis preloaded with products and stock counters.");
    }
}
