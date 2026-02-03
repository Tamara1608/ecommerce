package com.example.ecommerce.Product;

import com.example.ecommerce.Product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for Category entity.
 */
public interface CategoryTable extends JpaRepository<Category, Long> {
}

