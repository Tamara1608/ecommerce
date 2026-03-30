package com.example.ecommerce.category.app;

import com.example.ecommerce.category.api.dto.CategoryDTO;
import com.example.ecommerce.category.domain.Category;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Service interface for Category operations.
 */
public interface ICategoryService {
    
    @NonNull
    Category create(@NonNull Category category);
    
    @NonNull
    List<Category> findAll();
    
    @NonNull
    List<CategoryDTO> findAllDTO();

    @NonNull
    Category findById(@NonNull Long id);
    
    @NonNull
    CategoryDTO findByIdDTO(@NonNull Long id);

    @NonNull
    Category update(@NonNull Category category);
    
    void delete(@NonNull Long id);
}

