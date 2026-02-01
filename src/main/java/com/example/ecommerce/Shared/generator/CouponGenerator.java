package com.example.ecommerce.Shared.generator;

import com.example.ecommerce.Coupon.CouponTable;
import com.example.ecommerce.Order.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CouponGenerator implements CommandLineRunner {

    private final CouponTable couponTable;

    @Override
    public void run(String... args) {
        if (couponTable.count() > 0) {
            return;
        }
        int totalCoupons = 1000;

        for (int i = 0; i < totalCoupons; i++) {
            Coupon coupon = new Coupon();
            coupon.setCode(generateCode()); // unique code
            coupon.setDiscount(10); // fixed 10% off
            coupon.setValidUntil(LocalDateTime.now().plusDays(7)); // 7-day validity
            coupon.setUsed(false);

            couponTable.save(coupon);
        }

        System.out.println(totalCoupons + " coupons generated.");
    }

    private String generateCode() {
        return "FLASHSALE-" + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase();
    }
}
