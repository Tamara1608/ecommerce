package com.example.ecommerce.product.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ecommerce.category.infrastructure.persistence.category.CategoryTable;
import com.example.ecommerce.product.app.ProductService;
import com.example.ecommerce.product.infrastructure.persistence.product.IProductRepository;


@Configuration
public class ProductServiceConfig {
    
    @Bean
    @Qualifier("dbProductService")
    public ProductService dbProductService(
            @Qualifier("dbProductRepository") IProductRepository repository,
            CategoryTable categoryTable) {
        return new ProductService(repository, categoryTable);
    }
    
    @Bean
    @Qualifier("cachedProductService")
    public ProductService cachedProductService(
            @Qualifier("cachedProductRepository") IProductRepository repository,
            CategoryTable categoryTable) {
        return new ProductService(repository, categoryTable);
    }
}

