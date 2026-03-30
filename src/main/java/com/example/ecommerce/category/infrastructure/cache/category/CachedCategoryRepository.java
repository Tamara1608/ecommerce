package com.example.ecommerce.category.infrastructure.cache.category;

import com.example.ecommerce.category.api.dto.CategoryDTO;
import com.example.ecommerce.category.domain.Category;
import com.example.ecommerce.category.infrastructure.persistence.category.CategoryTable;
import com.example.ecommerce.category.infrastructure.persistence.category.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
@Slf4j
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
        return findAllDTO().stream()
                .map(this::dtoToCategory)
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public List<CategoryDTO> findAllDTO() {
        // Step 1: Check cache
        try {
            List<CategoryDTO> cachedDTOs = (List<CategoryDTO>) redisTemplate.opsForValue().get(ALL_CATEGORIES_KEY);
            if (cachedDTOs != null) {
                return cachedDTOs;
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize cached categories list, invalidating cache: {}", e.getMessage());
            invalidateAllCategoriesCache();
        }

        // Step 2: Cache miss - fetch from database
        List<Category> categories = categoryTable.findAll();

        // Step 3: Populate cache (read-through)
        List<CategoryDTO> dtos = categories.stream()
                .map(this::categoryToDTO)
                .collect(Collectors.toList());

        if (!dtos.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_CATEGORIES_KEY, dtos);
            // Also cache individual categories for consistency
            categories.forEach(this::cacheCategory);
        }

        return dtos;
    }

    /**
     * Read-Through: Check cache first, fetch from DB on miss, then cache.
     */
    @Override
    @NonNull
    public Optional<Category> findById(@NonNull Long id) {
        return findByIdDTO(id).map(this::dtoToCategory);
    }

    @Override
    @NonNull
    public Optional<CategoryDTO> findByIdDTO(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof CategoryDTO dto) {
                return Optional.of(dto);
            }
            if (cached instanceof Category category) {
                return Optional.of(categoryToDTO(category));
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize cached category {}, evicting from cache: {}", id, e.getMessage());
            evictFromCache(id);
        }

        // Cache miss - fetch from database
        Optional<Category> categoryOpt = categoryTable.findById(id);
        categoryOpt.ifPresent(this::cacheCategory);
        return categoryOpt.map(this::categoryToDTO);
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
        redisTemplate.opsForValue().set(cacheKey, categoryToDTO(category));
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void invalidateAllCategoriesCache() {
        redisTemplate.delete(ALL_CATEGORIES_KEY);
    }

    private CategoryDTO categoryToDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }

    private Category dtoToCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        return category;
    }
}
