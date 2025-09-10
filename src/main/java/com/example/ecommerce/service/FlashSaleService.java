package com.example.ecommerce.service;

import com.example.ecommerce.entity.Orders;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FlashSaleService {
   private final String STOCK_KEY_PREFIX = "stock:";
   private final RedisTemplate<String, Object> redisTemplate;
   private final OrderRepository orderRepository;

//    public FlashSaleService(RedisTemplate<String, Object> redisTemplate){
//        this.redisTemplate = redisTemplate;
//    }

    public String buyProduct(Long productId, Long userId) {
       String stockKey = STOCK_KEY_PREFIX + productId;
       Long stock = redisTemplate.opsForValue().decrement(stockKey);

       if (stock != null && stock >= 0) {
           Orders order = new Orders(null, productId, userId, 1, LocalDateTime.now());
           orderRepository.save(order);
           return "Order placed successfully!";
       } else {
           return "Sold Out!";
       }
    }
}
