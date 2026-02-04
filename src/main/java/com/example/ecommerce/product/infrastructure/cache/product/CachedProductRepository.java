package com.example.ecommerce.product.infrastructure.cache.product;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.infrastructure.persistence.product.IProductRepository;
import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;

import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("cachedProductRepository")
@RequiredArgsConstructor
public class CachedProductRepository implements IProductRepository {
    
    private final ProductTable productTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final String ALL_PRODUCTS_KEY = "products:all";
    private static final String STOCK_KEY_PREFIX = "stock:";
    
    @Override
    @NonNull
    public Product create(@NonNull Product product) {
        Product saved = productTable.save(product);
        cacheProduct(saved);
        invalidateAllProductsCache();
        return saved;
    }
    
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Product> findAll() {
        List<Product> cached = (List<Product>) redisTemplate.opsForValue().get(ALL_PRODUCTS_KEY);
        
        if (cached != null) {
            return cached;
        }
        
        List<Product> products = productTable.findAllWithStock();
        
        return products;
    }
    
    @Override
    @NonNull
    public Optional<Product> findById(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((Product) cached);
        }
        
        Optional<Product> productOpt = productTable.findById(id);
        
        productOpt.ifPresent(this::cacheProduct);
        
        return productOpt;
    }
    
    @Override
    @NonNull
    public Product update(@NonNull Product product) {
        Product updated = productTable.save(product);
        cacheProduct(updated);
        invalidateAllProductsCache();
        return updated;
    }
    
    @Override
    public void delete(@NonNull Long id) {
        productTable.deleteById(id);
        evictFromCache(id);
        invalidateAllProductsCache();
    }
    
    @Override
    @NonNull
    public Optional<Product> returnIfInStock(@NonNull Long productId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        Long stock = redisTemplate.opsForValue().decrement(stockKey, quantity);
        
        if (stock != null && stock >= 0) {
            String cacheKey = CACHE_KEY_PREFIX + productId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                return Optional.of((Product) cached);
            }
            
            Optional<Product> productOpt = productTable.findById(productId);
            productOpt.ifPresent(this::cacheProduct);
            
            return productOpt;
        } else {
            redisTemplate.opsForValue().increment(stockKey, quantity);
            return Optional.empty();
        }
    }
        
    private void cacheProduct(Product product) {
        String cacheKey = CACHE_KEY_PREFIX + product.getId();
        redisTemplate.opsForValue().set(cacheKey, product);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    
    private void invalidateAllProductsCache() {
        redisTemplate.delete(ALL_PRODUCTS_KEY);
    }
}

