package com.example.ecommerce.order.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ecommerce.order.app.OrderService;
import com.example.ecommerce.order.infrastructure.persistence.order.IOrderRepository;

@Configuration
public class OrderServiceConfig {
    
    @Bean
    @Qualifier("dbOrderService")
    public OrderService dbOrderService(
            @Qualifier("dbOrderRepository") IOrderRepository repository) {
        return new OrderService(repository);
    }
    
    @Bean
    @Qualifier("cachedOrderService")
    public OrderService cachedOrderService(
            @Qualifier("cachedOrderRepository") IOrderRepository repository) {
        return new OrderService(repository);
    }
}

