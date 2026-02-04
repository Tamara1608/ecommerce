package com.example.ecommerce.category.infrastructure.cache.category;

import com.example.ecommerce.category.domain.Category;
import com.example.ecommerce.category.infrastructure.persistence.category.ICategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Cached implementation of ICategoryRepository.
 * Uses Redis caching with Spring Cache annotations.
 */
@Repository
@Qualifier("cachedCategoryRepository")
@RequiredArgsConstructor
public class CachedCategoryRepository implements ICategoryRepository {

    private final ICategoryRepository dbCategoryRepository;

    @Override
    @NonNull
    @CachePut(value = "categories", key = "#result.id")
    public Category create(@NonNull Category category) {
        return dbCategoryRepository.create(category);
    }

    @Override
    @NonNull
    @Cacheable(value = "categories", key = "'all'")
    public List<Category> findAll() {
        return dbCategoryRepository.findAll();
    }

    @Override
    @NonNull
    @Cacheable(value = "categories", key = "#id")
    public Optional<Category> findById(@NonNull Long id) {
        return dbCategoryRepository.findById(id);
    }

    @Override
    @NonNull
    @CachePut(value = "categories", key = "#category.id")
    @CacheEvict(value = "categories", key = "'all'")
    public Category update(@NonNull Category category) {
        return dbCategoryRepository.update(category);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(@NonNull Long id) {
        dbCategoryRepository.delete(id);
    }
}

