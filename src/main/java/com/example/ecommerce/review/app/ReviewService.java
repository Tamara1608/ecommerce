package com.example.ecommerce.review.app;

import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;
import com.example.ecommerce.review.api.dto.ReviewCreateRequest;
import com.example.ecommerce.review.api.dto.ReviewDTO;
import com.example.ecommerce.review.domain.Review;
import com.example.ecommerce.review.infrastructure.persistence.review.IReviewRepository;
import com.example.ecommerce.user.domain.User;
import com.example.ecommerce.user.infrastructure.persistence.user.UserTable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewService implements IReviewService {
    
    private final IReviewRepository reviewRepository;
    private final UserTable userTable;
    private final ProductTable productTable;
    
    public ReviewService(IReviewRepository reviewRepository, UserTable userTable, ProductTable productTable) {
        this.reviewRepository = reviewRepository;
        this.userTable = userTable;
        this.productTable = productTable;
    }
    
    @Override
    @NonNull
    public ReviewDTO create(@NonNull ReviewCreateRequest request) {
        validateGrade(request.getGrade());
        
        if (reviewRepository.existsByUserAndProduct(request.getUserId(), request.getProductId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User has already reviewed this product");
        }
        
        User user = userTable.findById(request.getUserId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        Product product = productTable.findById(request.getProductId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        
        Review review = new Review();
        review.setComment(request.getComment());
        review.setGrade(request.getGrade());
        review.setUser(user);
        review.setProduct(product);
        
        Review saved = reviewRepository.create(review);
        return toDTO(saved);
    }
    
    @Override
    @NonNull
    public List<ReviewDTO> findAll() {
        return reviewRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @NonNull
    public ReviewDTO findById(@NonNull Long id) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        return toDTO(review);
    }
    
    @Override
    @NonNull
    public List<ReviewDTO> findByProductId(@NonNull Long productId) {
        return reviewRepository.findByProductId(productId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @NonNull
    public List<ReviewDTO> findByUserId(@NonNull Long userId) {
        return reviewRepository.findByUserId(userId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @NonNull
    public ReviewDTO update(@NonNull Long id, @NonNull ReviewCreateRequest request) {
        validateGrade(request.getGrade());
        
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        
        review.setComment(request.getComment());
        review.setGrade(request.getGrade());
        
        Review updated = reviewRepository.update(review);
        return toDTO(updated);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        reviewRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        reviewRepository.delete(id);
    }
    
    @Override
    public Double getAverageGradeByProductId(@NonNull Long productId) {
        return reviewRepository.getAverageGradeByProductId(productId);
    }
    
    @Override
    public Long getReviewCountByProductId(@NonNull Long productId) {
        return reviewRepository.countByProductId(productId);
    }
    
    private void validateGrade(Integer grade) {
        if (grade == null || grade < 1 || grade > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grade must be between 1 and 5");
        }
    }
    
    private ReviewDTO toDTO(Review review) {
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

