package com.example.ecommerce.order.app;

import com.example.ecommerce.order.api.dto.OrderDTO;
import com.example.ecommerce.order.domain.Order;
import com.example.ecommerce.order.domain.OrderItem;
import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.user.domain.User;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    
    @NonNull
    Order create(@NonNull Order order);
    
    @NonNull
    List<Order> findAll();
    
    @NonNull
    Optional<Order> findById(@NonNull Long id);
    
    @NonNull
    List<Order> findByUserId(@NonNull Long userId);
    
    @NonNull
    Order createOrderForUser(@NonNull User user, @NonNull List<OrderItem> items);
    
    @NonNull
    List<OrderDTO> getAllOrdersDTO();
    
    @NonNull
    OrderDTO getOrderByIdDTO(@NonNull Long id);
    
    @NonNull
    List<OrderDTO> getOrdersByUserDTO(@NonNull Long userId);
}

