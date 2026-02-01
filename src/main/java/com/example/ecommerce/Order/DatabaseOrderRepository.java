package com.example.ecommerce.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.Order.entity.Order;

import java.util.List;
import java.util.Optional;

/**
 * Repository implementation for Order CRUD operations.
 * Uses OrderTable (JPA repository) to perform database operations.
 * Direct database access without caching.
 */
@Repository
@Qualifier("dbOrderRepository")
@RequiredArgsConstructor
public class DatabaseOrderRepository implements IOrderRepository {
    
    private final OrderTable orderTable;
    
    @Override
    @NonNull
    public Order create(@NonNull Order order) {
        return orderTable.save(order);
    }
    
    @Override
    @NonNull
    public List<Order> findAll() {
        return orderTable.findAll();
    }
    
    @Override
    @NonNull
    public Optional<Order> findById(@NonNull Long id) {
        return orderTable.findById(id);
    }
    
    @Override
    @NonNull
    public List<Order> findByUserId(@NonNull Long userId) {
        return orderTable.findOrdersWithItemsAndProductsByUserId(userId);
    }
    
    @Override
    @NonNull
    public Order update(@NonNull Order order) {
        return orderTable.save(order);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        orderTable.deleteById(id);
    }
}

