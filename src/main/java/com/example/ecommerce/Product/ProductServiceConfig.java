package com.example.ecommerce.Product;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ProductService beans.
 * Creates two service instances - one with caching, one without.
 */
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

