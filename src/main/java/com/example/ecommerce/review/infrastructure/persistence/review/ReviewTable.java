package com.example.ecommerce.review.infrastructure.persistence.review;

import com.example.ecommerce.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewTable extends JpaRepository<Review, Long> {
    
    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.product WHERE r.product.id = :productId")
    List<Review> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.product WHERE r.user.id = :userId")
    List<Review> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT AVG(r.grade) FROM Review r WHERE r.product.id = :productId")
    Double getAverageGradeByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countByProductId(@Param("productId") Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}

