package com.example.ecommerce.controller;

import com.example.ecommerce.DTO.BuyRequest;
import com.example.ecommerce.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashsale")
@RequiredArgsConstructor
public class FlashSaleController {
    private final FlashSaleService flashSaleService;

    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestBody BuyRequest request) {
        try {
            String result = flashSaleService.buyProducts(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


