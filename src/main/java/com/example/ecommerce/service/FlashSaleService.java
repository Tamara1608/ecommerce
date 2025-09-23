package com.example.ecommerce.service;
import com.example.ecommerce.DTO.BuyRequest;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashSaleService {
    private final String STOCK_KEY_PREFIX = "stock:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public String buyProducts(BuyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        for (BuyRequest.ProductOrder po : request.getProducts()) {
            String stockKey = STOCK_KEY_PREFIX + po.getProductId();

            // Atomic decrement in Redis
            Long stock = redisTemplate.opsForValue().decrement(stockKey, po.getQuantity());

            if (stock != null && stock >= 0) {
                Product product = productRepository.findById(po.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProduct(product);
                item.setQuantity(po.getQuantity());

                items.add(item);

                result.append("Ordered ").append(po.getQuantity())
                        .append("x ").append(product.getName())
                        .append(". Remaining stock: ").append(stock).append("\n");
            } else {
                // rollback decrement if sold out
                redisTemplate.opsForValue().increment(stockKey, po.getQuantity());
                result.append(productRepository.findById(po.getProductId())
                                .map(Product::getName).orElse("Unknown"))
                        .append(" is Sold Out!\n");
            }
        }

        order.setItems(items);
        orderRepository.save(order);

        return result.toString();
    }

}

