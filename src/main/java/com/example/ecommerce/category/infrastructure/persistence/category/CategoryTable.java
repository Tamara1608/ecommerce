package com.example.ecommerce.category.infrastructure.persistence.category;

import com.example.ecommerce.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for Category entity.
 */
public interface CategoryTable extends JpaRepository<Category, Long> {
}

