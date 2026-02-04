package com.example.ecommerce.category.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ecommerce.category.app.CategoryService;
import com.example.ecommerce.category.infrastructure.persistence.category.ICategoryRepository;


@Configuration
public class CategoryServiceConfig {
    
    @Bean
    @Qualifier("dbCategoryService")
    public CategoryService dbCategoryService(
            @Qualifier("dbCategoryRepository") ICategoryRepository repository) {
        return new CategoryService(repository);
    }
    
    @Bean
    @Qualifier("cachedCategoryService")
    public CategoryService cachedCategoryService(
            @Qualifier("cachedCategoryRepository") ICategoryRepository repository) {
        return new CategoryService(repository);
    }
}

