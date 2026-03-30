package com.example.ecommerce.category.infrastructure.persistence.category;

import com.example.ecommerce.category.api.dto.CategoryDTO;
import com.example.ecommerce.category.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository implementation for Category CRUD operations.
 * Uses CategoryTable (JPA repository) to perform database operations.
 * Direct database access without caching.
 */
@Repository
@Qualifier("dbCategoryRepository")
@RequiredArgsConstructor
public class DatabaseCategoryRepository implements ICategoryRepository {

    private final CategoryTable categoryTable;

    @Override
    @NonNull
    public Category create(@NonNull Category category) {
        return categoryTable.save(category);
    }

    @Override
    @NonNull
    public List<Category> findAll() {
        return categoryTable.findAll();
    }

    @Override
    @NonNull
    public List<CategoryDTO> findAllDTO() {
        return categoryTable.findAll().stream()
                .map(this::categoryToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<Category> findById(@NonNull Long id) {
        return categoryTable.findById(id);
    }

    @Override
    @NonNull
    public Optional<CategoryDTO> findByIdDTO(@NonNull Long id) {
        return categoryTable.findById(id).map(this::categoryToDTO);
    }

    @Override
    @NonNull
    public Category update(@NonNull Category category) {
        return categoryTable.save(category);
    }

    @Override
    public void delete(@NonNull Long id) {
        categoryTable.deleteById(id);
    }

    private CategoryDTO categoryToDTO(Category category) {
        return new CategoryDTO(category.getId(), category.getName());
    }
}

