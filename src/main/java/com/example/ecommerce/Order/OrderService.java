package com.example.ecommerce.Order;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.User;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Order operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class OrderService implements IOrderService {
    
    private final IOrderRepository orderRepository;
    
    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    @NonNull
    public Order create(@NonNull Order order) {
        return orderRepository.create(order);
    }
    
    @Override
    @NonNull
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    @Override
    @NonNull
    public Optional<Order> findById(@NonNull Long id) {
        return orderRepository.findById(id);
    }
    
    @Override
    @NonNull
    public List<Order> findByUserId(@NonNull Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    @Override
    @NonNull
    public Order createOrderForUser(@NonNull User user, @NonNull List<OrderItem> items) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setItems(items);
        
        // Calculate total cost
        double totalCost = items.stream()
                .mapToDouble(OrderItem::getOrderItemPrice)
                .sum();
        order.setTotalCost(totalCost);
        
        return orderRepository.create(order);
    }
}

