package com.example.ecommerce.review.app;

import com.example.ecommerce.review.api.dto.ReviewCreateRequest;
import com.example.ecommerce.review.api.dto.ReviewDTO;
import org.springframework.lang.NonNull;

import java.util.List;

public interface IReviewService {
    
    @NonNull
    ReviewDTO create(@NonNull ReviewCreateRequest request);
    
    @NonNull
    List<ReviewDTO> findAll();
    
    @NonNull
    ReviewDTO findById(@NonNull Long id);
    
    @NonNull
    List<ReviewDTO> findByProductId(@NonNull Long productId);
    
    @NonNull
    List<ReviewDTO> findByUserId(@NonNull Long userId);
    
    @NonNull
    ReviewDTO update(@NonNull Long id, @NonNull ReviewCreateRequest request);
    
    void delete(@NonNull Long id);
    
    Double getAverageGradeByProductId(@NonNull Long productId);
    
    Long getReviewCountByProductId(@NonNull Long productId);
}

