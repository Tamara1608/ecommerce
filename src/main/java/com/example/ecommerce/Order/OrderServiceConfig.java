package com.example.ecommerce.Order;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OrderService beans.
 * Creates two service instances - one with caching, one without.
 */
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

