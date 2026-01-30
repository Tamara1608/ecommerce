package com.example.ecommerce.FlashSale;

import com.example.ecommerce.DTO.BuyRequest;
import com.example.ecommerce.entity.FlashSaleEvent;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    private final UserRepository userRepository;
    
    public FlashSaleBenchmarkController(
            @Qualifier("dbFlashSaleService") FlashSaleService dbFlashSaleService,
            @Qualifier("cachedFlashSaleService") FlashSaleService cachedFlashSaleService,
            UserRepository userRepository) {
        this.dbFlashSaleService = dbFlashSaleService;
        this.cachedFlashSaleService = cachedFlashSaleService;
        this.userRepository = userRepository;
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
    public ResponseEntity<FlashSaleEvent> dbCreate(@RequestBody FlashSaleEvent flashSale) {
        FlashSaleEvent created = dbFlashSaleService.create(flashSale);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/db/flashsales/{id}")
    public FlashSaleEvent dbUpdate(@PathVariable Long id, @RequestBody FlashSaleEvent flashSale) {
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
        User user = userRepository.findById(request.getUserId())
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
    public ResponseEntity<FlashSaleEvent> cachedCreate(@RequestBody FlashSaleEvent flashSale) {
        FlashSaleEvent created = cachedFlashSaleService.create(flashSale);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/cached/flashsales/{id}")
    public FlashSaleEvent cachedUpdate(@PathVariable Long id, @RequestBody FlashSaleEvent flashSale) {
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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found with id: " + request.getUserId()
                ));
        cachedFlashSaleService.buyProducts(user, request.getProducts());
        return ResponseEntity.ok().build();
    }
}

