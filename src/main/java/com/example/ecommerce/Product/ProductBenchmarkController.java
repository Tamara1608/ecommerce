package com.example.ecommerce.Product;

import com.example.ecommerce.entity.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for benchmarking cached vs non-cached product operations.
 * 
 * Routes:
 * - /api/benchmark/db/*      - Direct database access (no cache)
 * - /api/benchmark/cached/*  - Redis cached access
 */
@RestController
@RequestMapping("/api/benchmark")
public class ProductBenchmarkController {
    
    private final ProductService dbProductService;
    private final ProductService cachedProductService;
    
    public ProductBenchmarkController(
            @Qualifier("dbProductService") ProductService dbProductService,
            @Qualifier("cachedProductService") ProductService cachedProductService) {
        this.dbProductService = dbProductService;
        this.cachedProductService = cachedProductService;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache)
    // ===========================================
    
    @GetMapping("/db/products")
    public List<Product> dbFindAll() {
        return dbProductService.findAll();
    }
    
    @GetMapping("/db/products/{id}")
    public Product dbFindById(@PathVariable Long id) {
        return dbProductService.findById(id);
    }
    
    @PostMapping("/db/products")
    public ResponseEntity<Product> dbCreate(@RequestBody Product product) {
        Product created = dbProductService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/db/products/{id}")
    public Product dbUpdate(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return dbProductService.update(product);
    }
    
    @DeleteMapping("/db/products/{id}")
    public ResponseEntity<Void> dbDelete(@PathVariable Long id) {
        dbProductService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache)
    // ===========================================
    
    @GetMapping("/cached/products")
    public List<Product> cachedFindAll() {
        return cachedProductService.findAll();
    }
    
    @GetMapping("/cached/products/{id}")
    public Product cachedFindById(@PathVariable Long id) {
        return cachedProductService.findById(id);
    }
    
    @PostMapping("/cached/products")
    public ResponseEntity<Product> cachedCreate(@RequestBody Product product) {
        Product created = cachedProductService.create(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/cached/products/{id}")
    public Product cachedUpdate(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return cachedProductService.update(product);
    }
    
    @DeleteMapping("/cached/products/{id}")
    public ResponseEntity<Void> cachedDelete(@PathVariable Long id) {
        cachedProductService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

