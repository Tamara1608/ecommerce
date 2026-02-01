package com.example.ecommerce.Order;

import com.example.ecommerce.Order.OrderDTO;
import com.example.ecommerce.Order.entity.Order;
import com.example.ecommerce.Order.entity.OrderItem;
import com.example.ecommerce.User.entity.User;

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
    
    // -------------------
    // DTO Operations
    // -------------------
    
    /**
     * Get all orders as DTOs.
     */
    @NonNull
    List<OrderDTO> getAllOrdersDTO();
    
    /**
     * Get order by ID as DTO.
     */
    @NonNull
    OrderDTO getOrderByIdDTO(@NonNull Long id);
    
    /**
     * Get orders by user ID as DTOs.
     */
    @NonNull
    List<OrderDTO> getOrdersByUserDTO(@NonNull Long userId);
}

