package com.example.ecommerce.category.infrastructure.cache.category;

import com.example.ecommerce.category.domain.Category;
import com.example.ecommerce.category.infrastructure.persistence.category.CategoryTable;
import com.example.ecommerce.category.infrastructure.persistence.category.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Cached implementation of ICategoryRepository.
 * Implements both Write-Through and Read-Through caching strategies.
 * 
 * Write-Through: On create/update, data is written to DB first, then cached.
 * Read-Through: On read, cache is checked first. On miss, data is fetched from DB and cached.
 */
@Repository
@Qualifier("cachedCategoryRepository")
@RequiredArgsConstructor
public class CachedCategoryRepository implements ICategoryRepository {

    private final CategoryTable categoryTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "category:";
    private static final String ALL_CATEGORIES_KEY = "categories:all";

    /**
     * Write-Through: Create category in DB, then cache it.
     */
    @Override
    @NonNull
    public Category create(@NonNull Category category) {
        // Step 1: Write to database first
        Category saved = categoryTable.save(category);
        
        // Step 2: Write to cache (write-through)
        cacheCategory(saved);
        
        // Step 3: Invalidate all-categories cache for consistency
        invalidateAllCategoriesCache();
        
        return saved;
    }

    /**
     * Read-Through: Check cache first, fetch from DB on miss, then cache.
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Category> findAll() {
        // Step 1: Check cache
        List<Category> cached = (List<Category>) redisTemplate.opsForValue().get(ALL_CATEGORIES_KEY);
        
        if (cached != null) {
            return cached; // Cache hit
        }
        
        // Step 2: Cache miss - fetch from database
        List<Category> categories = categoryTable.findAll();
        
        // Step 3: Populate cache (read-through)
        if (!categories.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_CATEGORIES_KEY, categories);
            // Also cache individual categories for consistency
            categories.forEach(this::cacheCategory);
        }
        
        // Step 4: Return data
        return categories;
    }

    /**
     * Read-Through: Check cache first, fetch from DB on miss, then cache.
     */
    @Override
    @NonNull
    public Optional<Category> findById(@NonNull Long id) {
        // Step 1: Check cache
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((Category) cached); // Cache hit
        }
        
        // Step 2: Cache miss - fetch from database
        Optional<Category> categoryOpt = categoryTable.findById(id);
        
        // Step 3: Populate cache (read-through)
        categoryOpt.ifPresent(this::cacheCategory);
        
        // Step 4: Return data
        return categoryOpt;
    }

    /**
     * Write-Through: Update category in DB, then update cache.
     */
    @Override
    @NonNull
    public Category update(@NonNull Category category) {
        // Step 1: Write to database first
        Category updated = categoryTable.save(category);
        
        // Step 2: Update cache (write-through)
        cacheCategory(updated);
        
        // Step 3: Invalidate all-categories cache for consistency
        invalidateAllCategoriesCache();
        
        return updated;
    }

    /**
     * Delete from DB and evict from cache.
     */
    @Override
    public void delete(@NonNull Long id) {
        // Step 1: Delete from database
        categoryTable.deleteById(id);
        
        // Step 2: Evict from cache
        evictFromCache(id);
        
        // Step 3: Invalidate all-categories cache
        invalidateAllCategoriesCache();
    }
    
    // ========================================
    // Private Cache Helper Methods
    // ========================================
    
    private void cacheCategory(Category category) {
        String cacheKey = CACHE_KEY_PREFIX + category.getId();
        redisTemplate.opsForValue().set(cacheKey, category);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void invalidateAllCategoriesCache() {
        redisTemplate.delete(ALL_CATEGORIES_KEY);
    }
}
