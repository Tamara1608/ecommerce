package com.example.ecommerce.review.api;

import com.example.ecommerce.review.api.dto.ReviewCreateRequest;
import com.example.ecommerce.review.api.dto.ReviewDTO;
import com.example.ecommerce.review.app.ReviewService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/benchmark")
public class ReviewBenchmarkController {
    
    private final ReviewService dbReviewService;
    private final ReviewService cachedReviewService;
    
    public ReviewBenchmarkController(
            @Qualifier("dbReviewService") ReviewService dbReviewService,
            @Qualifier("cachedReviewService") ReviewService cachedReviewService) {
        this.dbReviewService = dbReviewService;
        this.cachedReviewService = cachedReviewService;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache)
    // ===========================================
    
    @PostMapping("/db/reviews")
    public ResponseEntity<ReviewDTO> dbCreate(@RequestBody ReviewCreateRequest request) {
        ReviewDTO created = dbReviewService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/db/reviews")
    public List<ReviewDTO> dbFindAll() {
        return dbReviewService.findAll();
    }
    
    @GetMapping("/db/reviews/{id}")
    public ReviewDTO dbFindById(@PathVariable Long id) {
        return dbReviewService.findById(id);
    }
    
    @GetMapping("/db/reviews/product/{productId}")
    public List<ReviewDTO> dbFindByProductId(@PathVariable Long productId) {
        return dbReviewService.findByProductId(productId);
    }
    
    @GetMapping("/db/reviews/user/{userId}")
    public List<ReviewDTO> dbFindByUserId(@PathVariable Long userId) {
        return dbReviewService.findByUserId(userId);
    }
    
    @GetMapping("/db/reviews/product/{productId}/stats")
    public Map<String, Object> dbGetProductStats(@PathVariable Long productId) {
        return Map.of(
            "productId", productId,
            "averageGrade", dbReviewService.getAverageGradeByProductId(productId),
            "reviewCount", dbReviewService.getReviewCountByProductId(productId)
        );
    }
    
    @PutMapping("/db/reviews/{id}")
    public ReviewDTO dbUpdate(@PathVariable Long id, @RequestBody ReviewCreateRequest request) {
        return dbReviewService.update(id, request);
    }
    
    @DeleteMapping("/db/reviews/{id}")
    public ResponseEntity<Map<String, Object>> dbDelete(@PathVariable Long id) {
        dbReviewService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully", "id", id));
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache)
    // ===========================================
    
    @PostMapping("/cached/reviews")
    public ResponseEntity<ReviewDTO> cachedCreate(@RequestBody ReviewCreateRequest request) {
        ReviewDTO created = cachedReviewService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/cached/reviews")
    public List<ReviewDTO> cachedFindAll() {
        return cachedReviewService.findAll();
    }
    
    @GetMapping("/cached/reviews/{id}")
    public ReviewDTO cachedFindById(@PathVariable Long id) {
        return cachedReviewService.findById(id);
    }
    
    @GetMapping("/cached/reviews/product/{productId}")
    public List<ReviewDTO> cachedFindByProductId(@PathVariable Long productId) {
        return cachedReviewService.findByProductId(productId);
    }
    
    @GetMapping("/cached/reviews/user/{userId}")
    public List<ReviewDTO> cachedFindByUserId(@PathVariable Long userId) {
        return cachedReviewService.findByUserId(userId);
    }
    
    @GetMapping("/cached/reviews/product/{productId}/stats")
    public Map<String, Object> cachedGetProductStats(@PathVariable Long productId) {
        return Map.of(
            "productId", productId,
            "averageGrade", cachedReviewService.getAverageGradeByProductId(productId),
            "reviewCount", cachedReviewService.getReviewCountByProductId(productId)
        );
    }
    
    @PutMapping("/cached/reviews/{id}")
    public ReviewDTO cachedUpdate(@PathVariable Long id, @RequestBody ReviewCreateRequest request) {
        return cachedReviewService.update(id, request);
    }
    
    @DeleteMapping("/cached/reviews/{id}")
    public ResponseEntity<Map<String, Object>> cachedDelete(@PathVariable Long id) {
        cachedReviewService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Review deleted successfully", "id", id));
    }
}

