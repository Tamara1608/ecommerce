package com.example.ecommerce.review.infrastructure.persistence.review;

import com.example.ecommerce.review.api.dto.ReviewDTO;
import com.example.ecommerce.review.domain.Review;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface IReviewRepository {
    
    @NonNull
    Review create(@NonNull Review review);
    
    @NonNull
    List<Review> findAll();
    
    @NonNull
    List<ReviewDTO> findAllDTO();
    
    @NonNull
    Optional<Review> findById(@NonNull Long id);
    
    @NonNull
    Optional<ReviewDTO> findByIdDTO(@NonNull Long id);
    
    @NonNull
    List<Review> findByProductId(@NonNull Long productId);
    
    @NonNull
    List<ReviewDTO> findByProductIdDTO(@NonNull Long productId);
    
    @NonNull
    List<Review> findByUserId(@NonNull Long userId);
    
    @NonNull
    List<ReviewDTO> findByUserIdDTO(@NonNull Long userId);
    
    @NonNull
    Review update(@NonNull Review review);
    
    void delete(@NonNull Long id);
    
    Double getAverageGradeByProductId(@NonNull Long productId);
    
    Long countByProductId(@NonNull Long productId);
    
    boolean existsByUserAndProduct(@NonNull Long userId, @NonNull Long productId);
}

