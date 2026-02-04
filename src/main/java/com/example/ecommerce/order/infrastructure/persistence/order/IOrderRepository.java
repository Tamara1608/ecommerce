package com.example.ecommerce.order.infrastructure.persistence.order;

import org.springframework.lang.NonNull;

import com.example.ecommerce.order.domain.Order;

import java.util.List;
import java.util.Optional;

public interface IOrderRepository {
    
    @NonNull
    Order create(@NonNull Order order);
    
    @NonNull
    List<Order> findAll();
    
    @NonNull
    Optional<Order> findById(@NonNull Long id);
    
    @NonNull
    List<Order> findByUserId(@NonNull Long userId);
    
    @NonNull
    Order update(@NonNull Order order);
    
    void delete(@NonNull Long id);
}

