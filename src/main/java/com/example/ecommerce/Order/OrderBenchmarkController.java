package com.example.ecommerce.Order;

import com.example.ecommerce.Order.OrderDTO;
import com.example.ecommerce.Order.entity.Order;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for benchmarking cached vs non-cached order operations.
 * 
 * Routes:
 * - /api/benchmark/db/orders/*      - Direct database access (no cache)
 * - /api/benchmark/cached/orders/*  - Redis cached access
 */
@RestController
@RequestMapping("/api/benchmark")
public class OrderBenchmarkController {
    
    private final OrderService dbOrderService;
    private final OrderService cachedOrderService;
    
    public OrderBenchmarkController(
            @Qualifier("dbOrderService") OrderService dbOrderService,
            @Qualifier("cachedOrderService") OrderService cachedOrderService) {
        this.dbOrderService = dbOrderService;
        this.cachedOrderService = cachedOrderService;
    }
    
    // ===========================================
    // DATABASE ROUTES (No Cache)
    // ===========================================
    
    @GetMapping("/db/orders")
    public ResponseEntity<List<OrderDTO>> dbGetAllOrders() {
        List<OrderDTO> orders = dbOrderService.getAllOrdersDTO();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/db/orders/{id}")
    public ResponseEntity<OrderDTO> dbGetOrderById(@PathVariable Long id) {
        OrderDTO order = dbOrderService.getOrderByIdDTO(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/db/orders/user/{userId}")
    public ResponseEntity<List<OrderDTO>> dbGetOrdersByUser(@PathVariable Long userId) {
        List<OrderDTO> orders = dbOrderService.getOrdersByUserDTO(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/db/orders/raw")
    public List<Order> dbFindAllRaw() {
        return dbOrderService.findAll();
    }
    
    // ===========================================
    // CACHED ROUTES (Redis Cache)
    // ===========================================
    
    @GetMapping("/cached/orders")
    public ResponseEntity<List<OrderDTO>> cachedGetAllOrders() {
        List<OrderDTO> orders = cachedOrderService.getAllOrdersDTO();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/cached/orders/{id}")
    public ResponseEntity<OrderDTO> cachedGetOrderById(@PathVariable Long id) {
        OrderDTO order = cachedOrderService.getOrderByIdDTO(id);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/cached/orders/user/{userId}")
    public ResponseEntity<List<OrderDTO>> cachedGetOrdersByUser(@PathVariable Long userId) {
        List<OrderDTO> orders = cachedOrderService.getOrdersByUserDTO(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/cached/orders/raw")
    public List<Order> cachedFindAllRaw() {
        return cachedOrderService.findAll();
    }
}

