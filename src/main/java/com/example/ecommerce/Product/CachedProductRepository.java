package com.example.ecommerce.Product;

import com.example.ecommerce.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cached repository implementation for Product CRUD operations.
 * Uses optimistic caching strategy - checks cache first, falls back to database.
 * Caches entire Product objects with key pattern: product:{productId}
 * All products are preloaded into cache by a cron job.
 */
@Repository
@Qualifier("cachedProductRepository")
@RequiredArgsConstructor
public class CachedProductRepository implements IProductRepository {
    
    private final ProductTable productTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final String ALL_PRODUCTS_KEY = "products:all";
    private static final String STOCK_KEY_PREFIX = "stock:";
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public Product create(@NonNull Product product) {
        Product saved = productTable.save(product);
        cacheProduct(saved);
        invalidateAllProductsCache();
        return saved;
    }
    
    // -------------------
    // READ operations
    // -------------------
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Product> findAll() {
        List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(ALL_PRODUCTS_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        // Cache miss - fetch from database
        List<Product> products = productTable.findAllWithStock();
        
        // Cache all products
        //cacheAllProducts(products);
        
        return products;
    }
    
    @Override
    @NonNull
    public Optional<Product> findById(@NonNull Long id) {
        // Check cache first (optimistic caching)
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((Product) cached);
        }
        
        // Cache miss - fetch from database
        Optional<Product> productOpt = productTable.findById(id);
        
        // Cache the result if found
        productOpt.ifPresent(this::cacheProduct);
        
        return productOpt;
    }
    
    // -------------------
    // UPDATE operations
    // -------------------
    @Override
    @NonNull
    public Product update(@NonNull Product product) {
        Product updated = productTable.save(product);
        cacheProduct(updated);
        invalidateAllProductsCache();
        return updated;
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        productTable.deleteById(id);
        evictFromCache(id);
        invalidateAllProductsCache();
    }
    
    // -------------------
    // STOCK operations
    // -------------------
    
    @Override
    @NonNull
    public Optional<Product> returnIfInStock(@NonNull Long productId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        Long stock = redisTemplate.opsForValue().decrement(stockKey, quantity);
        
        if (stock != null && stock >= 0) {
            // Stock available - get product from cache first
            String cacheKey = CACHE_KEY_PREFIX + productId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                return Optional.of((Product) cached);
            }
            
            // Product not in cache - fetch from database and update cache
            Optional<Product> productOpt = productTable.findById(productId);
            productOpt.ifPresent(this::cacheProduct);
            
            return productOpt;
        } else {
            // Sold out - rollback the decrement
            redisTemplate.opsForValue().increment(stockKey, quantity);
            return Optional.empty();
        }
    }
    
    // -------------------
    // Cache helper methods
    // -------------------
    
    private void cacheProduct(Product product) {
        String cacheKey = CACHE_KEY_PREFIX + product.getId();
        redisTemplate.opsForValue().set(cacheKey, product);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void cacheAllProducts(List<Product> products) {
        redisTemplate.opsForValue().set(ALL_PRODUCTS_KEY, products);
        // Also cache individual products
        products.forEach(this::cacheProduct);
    }
    
    private void invalidateAllProductsCache() {
        redisTemplate.delete(ALL_PRODUCTS_KEY);
    }
}

