package com.example.ecommerce.controller;

import com.example.ecommerce.entity.FlashSaleEvent;
import com.example.ecommerce.service.FlashSaleEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("flash-sales")
@RequiredArgsConstructor
public class FlashSaleEventController {

    private final FlashSaleEventService flashSaleEventService;

    @GetMapping
    public ResponseEntity<List<FlashSaleEvent>> getAllFlashSales() {
        return ResponseEntity.ok(flashSaleEventService.getAllFlashSales());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlashSaleEvent> getFlashSaleById(@PathVariable Long id) {
        return flashSaleEventService.getFlashSaleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FlashSaleEvent> createFlashSale(@RequestBody FlashSaleEvent flashSaleEvent) {
        FlashSaleEvent created = flashSaleEventService.createFlashSale(flashSaleEvent);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FlashSaleEvent> updateFlashSale(
            @PathVariable Long id,
            @RequestBody FlashSaleEvent flashSaleEvent) {
        FlashSaleEvent updated = flashSaleEventService.updateFlashSale(id, flashSaleEvent);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlashSale(@PathVariable Long id) {
        flashSaleEventService.deleteFlashSale(id);
        return ResponseEntity.noContent().build();
    }
}
