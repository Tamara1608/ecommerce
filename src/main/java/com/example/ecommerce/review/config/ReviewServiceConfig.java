package com.example.ecommerce.review.config;

import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;
import com.example.ecommerce.review.app.ReviewService;
import com.example.ecommerce.review.infrastructure.persistence.review.IReviewRepository;
import com.example.ecommerce.user.infrastructure.persistence.user.UserTable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewServiceConfig {
    
    @Bean
    @Qualifier("dbReviewService")
    public ReviewService dbReviewService(
            @Qualifier("dbReviewRepository") IReviewRepository repository,
            UserTable userTable,
            ProductTable productTable) {
        return new ReviewService(repository, userTable, productTable);
    }
    
    @Bean
    @Qualifier("cachedReviewService")
    public ReviewService cachedReviewService(
            @Qualifier("cachedReviewRepository") IReviewRepository repository,
            UserTable userTable,
            ProductTable productTable) {
        return new ReviewService(repository, userTable, productTable);
    }
}

