package com.example.ecommerce.FlashSale;

import com.example.ecommerce.FlashSale.BuyRequest;
import com.example.ecommerce.FlashSale.entity.FlashSaleEvent;
import com.example.ecommerce.User.IUserRepository;
import com.example.ecommerce.User.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Controller for benchmarking cached vs non-cached flash sale operations.
 * 
 * Routes:
 * - /api/benchmark/db/flashsales/*      - Direct database access (no cache)
 * - /api/benchmark/cached/flashsales/*  - Redis cached access
 */
@RestController
@RequestMapping("/api/benchmark")
public class FlashSaleBenchmarkController {
    
    private final FlashSaleService dbFlashSaleService;
    private final FlashSaleService cachedFlashSaleService;
    private final IUserRepository dbUserRepository;
    private final IUserRepository cachedUserRepository;
    
    public FlashSaleBenchmarkController(
            @Qualifier("dbFlashSaleService") FlashSaleService dbFlashSaleService,
            @Qualifier("cachedFlashSaleService") FlashSaleService cachedFlashSaleService,
            @Qualifier("dbUserRepository") IUserRepository dbUserRepository,
            @Qualifier("cachedUserRepository") IUserRepository cachedUserRepository) {
        this.dbFlashSaleService = dbFlashSaleService;
        this.cachedFlashSaleService = cachedFlashSaleService;
        this.dbUserRepository = dbUserRepository;
        this.cachedUserRepository = cachedUserRepository;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache)
    // ===========================================
    
    @GetMapping("/db/flashsales")
    public List<FlashSaleEvent> dbFindAll() {
        return dbFlashSaleService.findAll();
    }
    
    @GetMapping("/db/flashsales/{id}")
    public FlashSaleEvent dbFindById(@PathVariable Long id) {
        return dbFlashSaleService.findById(id);
    }
    
    @PostMapping("/db/flashsales")
    public ResponseEntity<Map<String, Object>> dbCreate(@RequestBody FlashSaleEvent flashSale) {
        Map<String, Object> result = dbFlashSaleService.create(flashSale);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @PutMapping("/db/flashsales/{id}")
    public Map<String, Object> dbUpdate(@PathVariable Long id, @RequestBody FlashSaleEvent flashSale) {
        flashSale.setId(id);
        return dbFlashSaleService.update(flashSale);
    }
    
    @DeleteMapping("/db/flashsales/{id}")
    public ResponseEntity<Void> dbDelete(@PathVariable Long id) {
        dbFlashSaleService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/db/flashsales/buy")
    public ResponseEntity<Void> dbBuy(@RequestBody BuyRequest request) {
        User user = dbUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + request.getUserId()
                ));
        dbFlashSaleService.buyProducts(user, request.getProducts());
        return ResponseEntity.ok().build();
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache)
    // ===========================================
    
    @GetMapping("/cached/flashsales")
    public List<FlashSaleEvent> cachedFindAll() {
        return cachedFlashSaleService.findAll();
    }
    
    @GetMapping("/cached/flashsales/{id}")
    public FlashSaleEvent cachedFindById(@PathVariable Long id) {
        return cachedFlashSaleService.findById(id);
    }
    
    @PostMapping("/cached/flashsales")
    public ResponseEntity<Map<String, Object>> cachedCreate(@RequestBody FlashSaleEvent flashSale) {
        Map<String, Object> result = cachedFlashSaleService.create(flashSale);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
    
    @PutMapping("/cached/flashsales/{id}")
    public Map<String, Object> cachedUpdate(@PathVariable Long id, @RequestBody FlashSaleEvent flashSale) {
        flashSale.setId(id);
        return cachedFlashSaleService.update(flashSale);
    }
    
    @DeleteMapping("/cached/flashsales/{id}")
    public ResponseEntity<Void> cachedDelete(@PathVariable Long id) {
        cachedFlashSaleService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/cached/flashsales/buy")
    public ResponseEntity<Void> cachedBuy(@RequestBody BuyRequest request) {
        User user = cachedUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + request.getUserId()
                ));
        cachedFlashSaleService.buyProducts(user, request.getProducts());
        return ResponseEntity.ok().build();
    }
    
    // ===========================================
    // ACTIVE FLASH SALE ROUTES (DB)
    // ===========================================
    
    @GetMapping("/db/flashsales/active")
    public List<FlashSaleEvent> dbGetActiveFlashSales() {
        return dbFlashSaleService.getActiveFlashSales();
    }
    
    @GetMapping("/db/flashsales/active/{id}")
    public ResponseEntity<FlashSaleEvent> dbGetActiveFlashSaleById(@PathVariable Long id) {
        return dbFlashSaleService.getActiveFlashSale(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/db/flashsales/check/active")
    public ResponseEntity<java.util.Map<String, Boolean>> dbHasActiveFlashSale() {
        boolean hasActive = dbFlashSaleService.hasActiveFlashSale();
        return ResponseEntity.ok(java.util.Map.of("hasActiveFlashSale", hasActive));
    }
    
    @GetMapping("/db/flashsales/product/{productId}/check")
    public ResponseEntity<java.util.Map<String, Boolean>> dbIsProductInFlashSale(@PathVariable Long productId) {
        boolean inFlashSale = dbFlashSaleService.isProductInActiveFlashSale(productId);
        return ResponseEntity.ok(java.util.Map.of("inFlashSale", inFlashSale));
    }
    
    @GetMapping("/db/flashsales/product/{productId}")
    public ResponseEntity<FlashSaleEvent> dbGetFlashSaleForProduct(@PathVariable Long productId) {
        return dbFlashSaleService.getFlashSaleForProduct(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // ===========================================
    // ACTIVE FLASH SALE ROUTES (Cached)
    // ===========================================
    
    @GetMapping("/cached/flashsales/active")
    public List<FlashSaleEvent> cachedGetActiveFlashSales() {
        return cachedFlashSaleService.getActiveFlashSales();
    }
    
    @GetMapping("/cached/flashsales/active/{id}")
    public ResponseEntity<FlashSaleEvent> cachedGetActiveFlashSaleById(@PathVariable Long id) {
        return cachedFlashSaleService.getActiveFlashSale(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cached/flashsales/check/active")
    public ResponseEntity<java.util.Map<String, Boolean>> cachedHasActiveFlashSale() {
        boolean hasActive = cachedFlashSaleService.hasActiveFlashSale();
        return ResponseEntity.ok(java.util.Map.of("hasActiveFlashSale", hasActive));
    }
    
    @GetMapping("/cached/flashsales/product/{productId}/check")
    public ResponseEntity<java.util.Map<String, Boolean>> cachedIsProductInFlashSale(@PathVariable Long productId) {
        boolean inFlashSale = cachedFlashSaleService.isProductInActiveFlashSale(productId);
        return ResponseEntity.ok(java.util.Map.of("inFlashSale", inFlashSale));
    }
    
    @GetMapping("/cached/flashsales/product/{productId}")
    public ResponseEntity<FlashSaleEvent> cachedGetFlashSaleForProduct(@PathVariable Long productId) {
        return cachedFlashSaleService.getFlashSaleForProduct(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

