package com.example.ecommerce.controller;

import com.example.ecommerce.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flashsale")
@RequiredArgsConstructor
public class FlashSaleController {
    private final FlashSaleService flashSaleService;

    @PostMapping("/buy")
    public String buy(@RequestParam Long productId, @RequestParam Long userId) {
        return flashSaleService.buyProduct(productId, userId);
    }
}
