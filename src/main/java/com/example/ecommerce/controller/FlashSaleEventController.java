package com.example.ecommerce.controller;

import com.example.ecommerce.entity.FlashSaleEvent;
import com.example.ecommerce.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("flash-sale-event")
@RequiredArgsConstructor
public class FlashSaleEventController {

    private final FlashSaleService flashSaleService;

    // ====================
    // Flash Sale CRUD Operations
    // ====================

    @GetMapping
    public ResponseEntity<List<FlashSaleEvent>> getAllFlashSales() {
        return ResponseEntity.ok(flashSaleService.getAllFlashSales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashSaleEvent> getFlashSaleById(@PathVariable Long id) {
        return flashSaleService.getFlashSaleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<FlashSaleEvent>> getActiveFlashSales() {
        return ResponseEntity.ok(flashSaleService.getActiveFlashSales());
    }

    @GetMapping("/active/{id}")
    public ResponseEntity<FlashSaleEvent> getActiveFlashSaleById(@PathVariable Long id) {
        return flashSaleService.getActiveFlashSale(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FlashSaleEvent> createFlashSale(@RequestBody FlashSaleEvent flashSaleEvent) {
        FlashSaleEvent created = flashSaleService.createFlashSale(flashSaleEvent);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashSaleEvent> updateFlashSale(
            @PathVariable Long id,
            @RequestBody FlashSaleEvent flashSaleEvent) {
        FlashSaleEvent updated = flashSaleService.updateFlashSale(id, flashSaleEvent);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable Long id) {
        flashSaleService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }

    // ====================
    // Flash Sale Query Operations
    // ====================

    @GetMapping("/check/active")
    public ResponseEntity<Map<String, Boolean>> hasActiveFlashSale() {
        boolean hasActive = flashSaleService.hasActiveFlashSale();
        return ResponseEntity.ok(Map.of("hasActiveFlashSale", hasActive));
    }

    @GetMapping("/product/{productId}/check")
    public ResponseEntity<Map<String, Boolean>> isProductInFlashSale(@PathVariable Long productId) {
        boolean inFlashSale = flashSaleService.isProductInActiveFlashSale(productId);
        return ResponseEntity.ok(Map.of("inFlashSale", inFlashSale));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<FlashSaleEvent> getFlashSaleForProduct(@PathVariable Long productId) {
        return flashSaleService.getFlashSaleForProduct(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
