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
    private final CouponService couponService;


    public String buyProducts(BuyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        StringBuilder result = new StringBuilder();
        double totalCost = 0;

        for (BuyRequest.ProductOrder po : request.getProducts()) {
            String stockKey = STOCK_KEY_PREFIX + po.getProductId();

            Long stock = redisTemplate.opsForValue().decrement(stockKey, po.getQuantity());

            if (stock != null && stock >= 0) {
                Product product = productRepository.findById(po.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem item = createOrderItem(order, product, po);

                items.add(item);

                result.append("Ordered ").append(po.getQuantity())
                        .append("x ").append(product.getName())
                        .append(". Remaining stock: ").append(stock).append("\n");

            } else { // if stock < 0 or null
                redisTemplate.opsForValue().increment(stockKey, po.getQuantity());
                result.append(productRepository.findById(po.getProductId())
                                .map(Product::getName).orElse("Unknown"))
                        .append(" is Sold Out!\n");
            }
        }
        // Should I involve coupons or not ?

        if (items.isEmpty()) {
            return "No products could be ordered. All sold out.";
        }
        order.setItems(items);
        orderRepository.save(order);

        return result.toString();
    }

    private OrderItem createOrderItem(Order o, Product p, BuyRequest.ProductOrder po){
        OrderItem item = new OrderItem();
        item.setOrder(o);
        item.setProduct(p);
        Integer quantity = po.getQuantity();
        item.setQuantity(quantity);
        double unitPrice = p.getPrice();
        item.setUnitPrice(unitPrice);
        item.setOrderItemPrice(unitPrice * quantity);

        return item;
    }

}

