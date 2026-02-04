package com.example.ecommerce.category.app;

import com.example.ecommerce.category.domain.Category;
import com.example.ecommerce.category.infrastructure.persistence.category.ICategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * Service for Category CRUD operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class CategoryService implements ICategoryService {
    
    private final ICategoryRepository categoryRepository;
    
    public CategoryService(ICategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @NonNull
    public Category create(@NonNull Category category) {
        return categoryRepository.create(category);
    }
    
    @NonNull
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @NonNull
    public Category findById(@NonNull Long id) {
        return Objects.requireNonNull(categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + id
                )));
    }
    
    @NonNull
    public Category update(@NonNull Category category) {
        Long categoryId = category.getId();
        if (categoryId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID is required for update");
        }
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + categoryId
                ));
        return categoryRepository.update(category);
    }
    
    public void delete(@NonNull Long id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Category not found with id: " + id
                ));
        categoryRepository.delete(id);
    }
}

