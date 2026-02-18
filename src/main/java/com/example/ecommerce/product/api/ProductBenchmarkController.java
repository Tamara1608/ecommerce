package com.example.ecommerce.product.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce.product.api.dto.ProductCreateRequest;
import com.example.ecommerce.product.api.dto.ProductDTO;
import com.example.ecommerce.product.api.dto.ProductUpdateRequest;
import com.example.ecommerce.product.app.ProductService;
import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.infrastructure.sync.PopularProductRefreshJob;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/benchmark")
public class ProductBenchmarkController {
    
    private final ProductService dbProductService;
    private final ProductService cachedProductService;
    private final PopularProductRefreshJob popularProductRefreshJob;
    
    public ProductBenchmarkController(
            @Qualifier("dbProductService") ProductService dbProductService,
            @Qualifier("cachedProductService") ProductService cachedProductService,
            PopularProductRefreshJob popularProductRefreshJob) {
        this.dbProductService = dbProductService;
        this.cachedProductService = cachedProductService;
        this.popularProductRefreshJob = popularProductRefreshJob;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache)
    // ===========================================
    
    @GetMapping("/db/products")
    public List<ProductDTO> dbFindAll() {
        return dbProductService.findAllDTO();
    }
    
    @GetMapping("/db/products/{id}")
    public Product dbFindById(@PathVariable Long id) {
        popularProductRefreshJob.trackProductView(id);
        return dbProductService.findById(id);
    }
    
    @PostMapping("/db/products")
    public ResponseEntity<Product> dbCreate(@RequestBody ProductCreateRequest request) {
        Product created = dbProductService.createWithDetails(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PatchMapping("/db/products/{id}")
    public Product dbUpdate(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        return dbProductService.updatePartial(id, request);
    }
    
    @DeleteMapping("/db/products/{id}")
    public ResponseEntity<Map<String, Object>> dbDelete(@PathVariable Long id) {
        dbProductService.delete(id);
        return ResponseEntity.ok(Map.of(
            "message", "Product deleted successfully",
            "id", id
        ));
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache)
    // ===========================================
    
    @GetMapping("/cached/products")
    public List<ProductDTO> cachedFindAll() {
        return cachedProductService.findAllDTO();
    }
    
    @GetMapping("/cached/products/{id}")
    public ProductDTO cachedFindById(@PathVariable Long id) {
        popularProductRefreshJob.trackProductView(id);
        return cachedProductService.findByIdDTO(id);
    }
    
    @PostMapping("/cached/products")
    public ResponseEntity<Product> cachedCreate(@RequestBody ProductCreateRequest request) {
        Product created = cachedProductService.createWithDetails(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PatchMapping("/cached/products/{id}")
    public Product cachedUpdate(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
        return cachedProductService.updatePartial(id, request);
    }
    
    @DeleteMapping("/cached/products/{id}")
    public ResponseEntity<Map<String, Object>> cachedDelete(@PathVariable Long id) {
        cachedProductService.delete(id);
        return ResponseEntity.ok(Map.of(
            "message", "Product deleted successfully",
            "id", id
        ));
    }
}

