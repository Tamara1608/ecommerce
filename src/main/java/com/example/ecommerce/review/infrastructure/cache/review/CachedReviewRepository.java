package com.example.ecommerce.review.infrastructure.cache.review;

import com.example.ecommerce.review.domain.Review;
import com.example.ecommerce.review.infrastructure.persistence.review.IReviewRepository;
import com.example.ecommerce.review.infrastructure.persistence.review.ReviewTable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Write-Around + Read-Through implementation for Reviews.
 * 
 * Write-Around: Reviews are written to DB only, skips cache on create/update
 * Read-Through: Reviews are cached on first read (lazy loading)
 * 
 * Why Write-Around?
 * - Reviews are created occasionally but read frequently
 * - Product reviews are fetched as a list, not individually
 * - Better to cache by product_id (list of reviews) than individual reviews
 */
@Repository
@Qualifier("cachedReviewRepository")
@RequiredArgsConstructor
public class CachedReviewRepository implements IReviewRepository {

    private final ReviewTable reviewTable;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_KEY_PREFIX = "review:";
    private static final String PRODUCT_REVIEWS_PREFIX = "reviews:product:";
    private static final String USER_REVIEWS_PREFIX = "reviews:user:";
    private static final String PRODUCT_AVG_PREFIX = "reviews:avg:";
    private static final String PRODUCT_COUNT_PREFIX = "reviews:count:";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    @NonNull
    public Review create(@NonNull Review review) {
        Review saved = reviewTable.save(review);
        
        invalidateProductReviewsCache(saved.getProduct().getId());
        invalidateUserReviewsCache(saved.getUser().getId());
        invalidateProductStatsCache(saved.getProduct().getId());
        
        return saved;
    }

    @Override
    @NonNull
    public List<Review> findAll() {
        return reviewTable.findAll();
    }

    @Override
    @NonNull
    public Optional<Review> findById(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return Optional.of((Review) cached);
        }
        
        Optional<Review> reviewOpt = reviewTable.findById(id);
        reviewOpt.ifPresent(review -> cacheReview(review));
        
        return reviewOpt;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Review> findByProductId(@NonNull Long productId) {
        String cacheKey = PRODUCT_REVIEWS_PREFIX + productId;
        List<Review> cached = (List<Review>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        List<Review> reviews = reviewTable.findByProductId(productId);
        
        if (!reviews.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, reviews, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        
        return reviews;
    }

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<Review> findByUserId(@NonNull Long userId) {
        String cacheKey = USER_REVIEWS_PREFIX + userId;
        List<Review> cached = (List<Review>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return cached;
        }
        
        List<Review> reviews = reviewTable.findByUserId(userId);
        
        if (!reviews.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, reviews, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        
        return reviews;
    }

    @Override
    @NonNull
    public Review update(@NonNull Review review) {
        Review updated = reviewTable.save(review);
        
        evictFromCache(updated.getId());
        invalidateProductReviewsCache(updated.getProduct().getId());
        invalidateUserReviewsCache(updated.getUser().getId());
        invalidateProductStatsCache(updated.getProduct().getId());
        
        return updated;
    }

    @Override
    public void delete(@NonNull Long id) {
        Optional<Review> reviewOpt = reviewTable.findById(id);
        reviewTable.deleteById(id);
        
        evictFromCache(id);
        reviewOpt.ifPresent(review -> {
            invalidateProductReviewsCache(review.getProduct().getId());
            invalidateUserReviewsCache(review.getUser().getId());
            invalidateProductStatsCache(review.getProduct().getId());
        });
    }

    @Override
    public Double getAverageGradeByProductId(@NonNull Long productId) {
        String cacheKey = PRODUCT_AVG_PREFIX + productId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return ((Number) cached).doubleValue();
        }
        
        Double avg = reviewTable.getAverageGradeByProductId(productId);
        
        if (avg != null) {
            redisTemplate.opsForValue().set(cacheKey, avg, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        
        return avg;
    }

    @Override
    public Long countByProductId(@NonNull Long productId) {
        String cacheKey = PRODUCT_COUNT_PREFIX + productId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return ((Number) cached).longValue();
        }
        
        Long count = reviewTable.countByProductId(productId);
        redisTemplate.opsForValue().set(cacheKey, count, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        
        return count;
    }

    @Override
    public boolean existsByUserAndProduct(@NonNull Long userId, @NonNull Long productId) {
        return reviewTable.existsByUserIdAndProductId(userId, productId);
    }
    
    private void cacheReview(Review review) {
        String cacheKey = CACHE_KEY_PREFIX + review.getId();
        redisTemplate.opsForValue().set(cacheKey, review, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
    }
    
    private void evictFromCache(Long id) {
        redisTemplate.delete(CACHE_KEY_PREFIX + id);
    }
    
    private void invalidateProductReviewsCache(Long productId) {
        redisTemplate.delete(PRODUCT_REVIEWS_PREFIX + productId);
    }
    
    private void invalidateUserReviewsCache(Long userId) {
        redisTemplate.delete(USER_REVIEWS_PREFIX + userId);
    }
    
    private void invalidateProductStatsCache(Long productId) {
        redisTemplate.delete(PRODUCT_AVG_PREFIX + productId);
        redisTemplate.delete(PRODUCT_COUNT_PREFIX + productId);
    }
}

