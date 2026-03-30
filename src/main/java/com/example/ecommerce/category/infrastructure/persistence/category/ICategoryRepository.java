package com.example.ecommerce.category.infrastructure.persistence.category;

import com.example.ecommerce.category.domain.Category;
import com.example.ecommerce.category.api.dto.CategoryDTO;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Category CRUD operations.
 */
public interface ICategoryRepository {
    
    @NonNull
    Category create(@NonNull Category category);
    
    @NonNull
    List<Category> findAll();

    @NonNull
    List<CategoryDTO> findAllDTO();
    
    @NonNull
    Optional<Category> findById(@NonNull Long id);

    @NonNull
    Optional<CategoryDTO> findByIdDTO(@NonNull Long id);
    
    @NonNull
    Category update(@NonNull Category category);
    
    void delete(@NonNull Long id);
}

