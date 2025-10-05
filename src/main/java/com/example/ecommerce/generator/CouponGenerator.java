package com.example.ecommerce.generator;

import com.example.ecommerce.entity.Coupon;
import com.example.ecommerce.repository.CouponRepository;
import com.example.ecommerce.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CouponGenerator implements CommandLineRunner {

    private final CouponRepository couponRepository;
    private final CouponService couponService;

    @Override
    public void run(String... args) {
        if (couponRepository.count() > 0) {
            return;
        }
        int totalCoupons = 1000;

        for (int i = 0; i < totalCoupons; i++) {
            Coupon coupon = new Coupon();
            coupon.setCode(generateCode()); // unique code
            coupon.setDiscount(10); // fixed 10% off
            coupon.setValidUntil(LocalDateTime.now().plusDays(7)); // 7-day validity
            coupon.setUsed(false);

            couponRepository.save(coupon);

            couponService.cacheCoupon(coupon);
        }

        System.out.println(totalCoupons + " coupons generated and cached.");
    }

    private String generateCode() {
        return "FLASHSALE-" + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
