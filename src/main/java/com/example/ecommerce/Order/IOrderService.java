package com.example.ecommerce.Order;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.User;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Order operations.
 */
public interface IOrderService {
    
    /**
     * Create a new order.
     */
    @NonNull
    Order create(@NonNull Order order);
    
    /**
     * Find all orders.
     */
    @NonNull
    List<Order> findAll();
    
    /**
     * Find an order by ID.
     */
    @NonNull
    Optional<Order> findById(@NonNull Long id);
    
    /**
     * Find orders by user ID with items and products.
     */
    @NonNull
    List<Order> findByUserId(@NonNull Long userId);
    
    /**
     * Create and save an order for a user with the given items.
     */
    @NonNull
    Order createOrderForUser(@NonNull User user, @NonNull List<OrderItem> items);
}

