package com.example.ecommerce.common.preloader;
//package com.example.ecommerce.preloader;
//
//import com.example.ecommerce.entity.Coupon;
//import com.example.ecommerce.service.CouponService;
//import lombok.AllArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@AllArgsConstructor
//public class CouponPreload implements CommandLineRunner {
//
//    private final CouponService couponService;
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final String COUPON_METADATA_PREFIX = "coupon:data:";
//    private final String COUPON_USAGE_PREFIX = "coupon:used:";
//
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        List<Coupon> coupons = couponService.getAllCoupons();
//
//        for (Coupon c : coupons) {
//            redisTemplate.opsForValue().set(COUPON_METADATA_PREFIX + c.getCode(), c);
//            redisTemplate.opsForSet().add(COUPON_USAGE_PREFIX, c.getCode());
//        }
//    }
//}
