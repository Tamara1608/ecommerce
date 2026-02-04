package com.example.ecommerce.order.api;

import com.example.ecommerce.order.api.dto.OrderDTO;
import com.example.ecommerce.order.app.OrderService;
import com.example.ecommerce.order.domain.Order;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

