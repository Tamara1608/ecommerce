package com.example.ecommerce.controller;

import com.example.ecommerce.DTO.BuyRequest;
import com.example.ecommerce.service.AuthService;
import com.example.ecommerce.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/flashsale")
@RequiredArgsConstructor
public class BuyFlashSaleProductsController {
    private final FlashSaleService flashSaleService;
    private final AuthService authService;


    @PostMapping("/buy")
    public ResponseEntity<String> buy(@RequestBody BuyRequest request, HttpSession session) {
        try {
            Long uid = authService.getCurrentUserId(session).orElse(null);
            if (uid == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
            }
            request.setUserId(uid);
            String result = flashSaleService.buyProducts(request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}


