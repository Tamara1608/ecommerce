package com.example.ecommerce.Coupon;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce.Order.entity.Coupon;

import java.util.List;


@RestController
@RequestMapping("/api/benchmark")
public class CouponBenchmarkController {
    
    private final CouponService dbCouponService;
    private final CouponService cachedCouponService;
    
    public CouponBenchmarkController(
            @Qualifier("dbCouponService") CouponService dbCouponService,
            @Qualifier("cachedCouponService") CouponService cachedCouponService) {
        this.dbCouponService = dbCouponService;
        this.cachedCouponService = cachedCouponService;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache) - CRUD
    // ===========================================
    
    @GetMapping("/db/coupons")
    public List<Coupon> dbFindAll() {
        return dbCouponService.findAll();
    }
    
    @GetMapping("/db/coupons/{id}")
    public Coupon dbFindById(@PathVariable Long id) {
        return dbCouponService.findById(id);
    }
    
    @GetMapping("/db/coupons/code/{code}")
    public ResponseEntity<Coupon> dbFindByCode(@PathVariable String code) {
        return dbCouponService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/db/coupons")
    public ResponseEntity<Coupon> dbCreate(@RequestBody Coupon coupon) {
        Coupon created = dbCouponService.create(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/db/coupons/{id}")
    public Coupon dbUpdate(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        return dbCouponService.update(coupon);
    }
    
    @DeleteMapping("/db/coupons/{id}")
    public ResponseEntity<Void> dbDelete(@PathVariable Long id) {
        dbCouponService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache) - Apply Coupon
    // ===========================================
    
    @PostMapping("/db/coupons/apply")
    public ResponseEntity<String> dbApplyCoupon(
            @RequestParam String code,
            @RequestParam Long userId) {
        boolean success = dbCouponService.applyCoupon(code, userId);
        if (success) {
            return ResponseEntity.ok("Coupon applied successfully");
        }
        return ResponseEntity.badRequest().body("Failed to apply coupon");
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache) - CRUD
    // ===========================================
    
    @GetMapping("/cached/coupons")
    public List<Coupon> cachedFindAll() {
        return cachedCouponService.findAll();
    }
    
    @GetMapping("/cached/coupons/{id}")
    public Coupon cachedFindById(@PathVariable Long id) {
        return cachedCouponService.findById(id);
    }
    
    @GetMapping("/cached/coupons/code/{code}")
    public ResponseEntity<Coupon> cachedFindByCode(@PathVariable String code) {
        return cachedCouponService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/cached/coupons")
    public ResponseEntity<Coupon> cachedCreate(@RequestBody Coupon coupon) {
        Coupon created = cachedCouponService.create(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/cached/coupons/{id}")
    public Coupon cachedUpdate(@PathVariable Long id, @RequestBody Coupon coupon) {
        coupon.setId(id);
        return cachedCouponService.update(coupon);
    }
    
    @DeleteMapping("/cached/coupons/{id}")
    public ResponseEntity<Void> cachedDelete(@PathVariable Long id) {
        cachedCouponService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache) - Apply Coupon
    // ===========================================
    
    @PostMapping("/cached/coupons/apply")
    public ResponseEntity<String> cachedApplyCoupon(
            @RequestParam String code,
            @RequestParam Long userId) {
        boolean success = cachedCouponService.applyCoupon(code, userId);
        if (success) {
            return ResponseEntity.ok("Coupon applied successfully");
        }
        return ResponseEntity.badRequest().body("Failed to apply coupon");
    }
}

