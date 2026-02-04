package com.example.ecommerce.category.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce.category.api.dto.CategoryCreateRequest;
import com.example.ecommerce.category.app.CategoryService;
import com.example.ecommerce.category.domain.Category;

import java.util.List;
import java.util.Map;

/**
 * Controller for benchmarking cached vs non-cached category operations.
 * 
 * Routes:
 * - /api/benchmark/db/*      - Direct database access (no cache)
 * - /api/benchmark/cached/*  - Redis cached access
 */
@RestController
@RequestMapping("/api/benchmark")
public class CategoryBenchmarkController {
    
    private final CategoryService dbCategoryService;
    private final CategoryService cachedCategoryService;
    
    public CategoryBenchmarkController(
            @Qualifier("dbCategoryService") CategoryService dbCategoryService,
            @Qualifier("cachedCategoryService") CategoryService cachedCategoryService) {
        this.dbCategoryService = dbCategoryService;
        this.cachedCategoryService = cachedCategoryService;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache)
    // ===========================================
    
    @GetMapping("/db/categories")
    public List<Category> dbFindAll() {
        return dbCategoryService.findAll();
    }
    
    @GetMapping("/db/categories/{id}")
    public Category dbFindById(@PathVariable Long id) {
        return dbCategoryService.findById(id);
    }
    
    @PostMapping("/db/categories")
    public ResponseEntity<Category> dbCreate(@RequestBody CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        Category created = dbCategoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/db/categories/{id}")
    public Category dbUpdate(@PathVariable Long id, @RequestBody CategoryCreateRequest request) {
        Category existingCategory = dbCategoryService.findById(id);
        existingCategory.setName(request.getName());
        return dbCategoryService.update(existingCategory);
    }
    
    @DeleteMapping("/db/categories/{id}")
    public ResponseEntity<Map<String, Object>> dbDelete(@PathVariable Long id) {
        dbCategoryService.delete(id);
        return ResponseEntity.ok(Map.of(
            "message", "Category deleted successfully",
            "id", id
        ));
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache)
    // ===========================================
    
    @GetMapping("/cached/categories")
    public List<Category> cachedFindAll() {
        return cachedCategoryService.findAll();
    }
    
    @GetMapping("/cached/categories/{id}")
    public Category cachedFindById(@PathVariable Long id) {
        return cachedCategoryService.findById(id);
    }
    
    @PostMapping("/cached/categories")
    public ResponseEntity<Category> cachedCreate(@RequestBody CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        Category created = cachedCategoryService.create(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/cached/categories/{id}")
    public Category cachedUpdate(@PathVariable Long id, @RequestBody CategoryCreateRequest request) {
        Category existingCategory = cachedCategoryService.findById(id);
        existingCategory.setName(request.getName());
        return cachedCategoryService.update(existingCategory);
    }
    
    @DeleteMapping("/cached/categories/{id}")
    public ResponseEntity<Map<String, Object>> cachedDelete(@PathVariable Long id) {
        cachedCategoryService.delete(id);
        return ResponseEntity.ok(Map.of(
            "message", "Category deleted successfully",
            "id", id
        ));
    }
}

