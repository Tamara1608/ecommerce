package com.example.ecommerce.review.infrastructure.persistence.review;

import com.example.ecommerce.review.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public Optional<Review> findById(@NonNull Long id) {
        return reviewTable.findById(id);
    }

    @Override
    @NonNull
    public List<Review> findByProductId(@NonNull Long productId) {
        return reviewTable.findByProductId(productId);
    }

    @Override
    @NonNull
    public List<Review> findByUserId(@NonNull Long userId) {
        return reviewTable.findByUserId(userId);
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
}

