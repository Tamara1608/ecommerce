package com.example.ecommerce.Order;

import com.example.ecommerce.entity.Order;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order CRUD operations.
 * All methods are stateful and result in permanent changes.
 */
public interface IOrderRepository {
    
    // -------------------
    // CREATE operations
    // -------------------
    
    /**
     * Create a new order.
     * Stateful operation - persists immediately.
     */
    @NonNull
    Order create(@NonNull Order order);
    
    // -------------------
    // READ operations
    // -------------------
    
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
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    /**
     * Update an existing order.
     * Stateful operation - persists immediately.
     */
    @NonNull
    Order update(@NonNull Order order);
    
    // -------------------
    // DELETE operations
    // -------------------
    
    /**
     * Delete an order by ID.
     * Stateful operation - deletes immediately.
     */
    void delete(@NonNull Long id);
}

