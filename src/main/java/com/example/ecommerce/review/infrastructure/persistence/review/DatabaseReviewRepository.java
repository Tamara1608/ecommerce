package com.example.ecommerce.review.infrastructure.persistence.review;

import com.example.ecommerce.review.api.dto.ReviewDTO;
import com.example.ecommerce.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Qualifier("dbReviewRepository")
@RequiredArgsConstructor
public class DatabaseReviewRepository implements IReviewRepository {

    private final ReviewTable reviewTable;

    @Override
    @NonNull
    public Review create(@NonNull Review review) {
        return reviewTable.save(review);
    }

    @Override
    @NonNull
    public List<Review> findAll() {
        return reviewTable.findAll();
    }
    
    @Override
    @NonNull
    public List<ReviewDTO> findAllDTO() {
        return reviewTable.findAll().stream()
                .map(this::reviewToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<Review> findById(@NonNull Long id) {
        return reviewTable.findById(id);
    }
    
    @Override
    @NonNull
    public Optional<ReviewDTO> findByIdDTO(@NonNull Long id) {
        return reviewTable.findById(id).map(this::reviewToDTO);
    }

    @Override
    @NonNull
    public List<Review> findByProductId(@NonNull Long productId) {
        return reviewTable.findByProductId(productId);
    }
    
    @Override
    @NonNull
    public List<ReviewDTO> findByProductIdDTO(@NonNull Long productId) {
        return reviewTable.findByProductId(productId).stream()
                .map(this::reviewToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public List<Review> findByUserId(@NonNull Long userId) {
        return reviewTable.findByUserId(userId);
    }
    
    @Override
    @NonNull
    public List<ReviewDTO> findByUserIdDTO(@NonNull Long userId) {
        return reviewTable.findByUserId(userId).stream()
                .map(this::reviewToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Review update(@NonNull Review review) {
        return reviewTable.save(review);
    }

    @Override
    public void delete(@NonNull Long id) {
        reviewTable.deleteById(id);
    }

    @Override
    public Double getAverageGradeByProductId(@NonNull Long productId) {
        return reviewTable.getAverageGradeByProductId(productId);
    }

    @Override
    public Long countByProductId(@NonNull Long productId) {
        return reviewTable.countByProductId(productId);
    }

    @Override
    public boolean existsByUserAndProduct(@NonNull Long userId, @NonNull Long productId) {
        return reviewTable.existsByUserIdAndProductId(userId, productId);
    }
    
    private ReviewDTO reviewToDTO(Review review) {
        return new ReviewDTO(
            review.getId(),
            review.getComment(),
            review.getGrade(),
            review.getUser().getId(),
            review.getUser().getUsername(),
            review.getProduct().getId(),
            review.getProduct().getName(),
            review.getCreatedAt()
        );
    }
}

