package com.example.ecommerce.Order;

import com.example.ecommerce.Order.OrderDTO;
import com.example.ecommerce.Order.OrderItemDTO;
import com.example.ecommerce.Order.entity.Order;
import com.example.ecommerce.Order.entity.OrderItem;
import com.example.ecommerce.Product.ProductDTO;
import com.example.ecommerce.User.UserDTO;
import com.example.ecommerce.User.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for Order operations.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class OrderService implements IOrderService {
    
    private final IOrderRepository orderRepository;
    
    public OrderService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    @Override
    @NonNull
    public Order create(@NonNull Order order) {
        return orderRepository.create(order);
    }
    
    @Override
    @NonNull
    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    
    @Override
    @NonNull
    public Optional<Order> findById(@NonNull Long id) {
        return orderRepository.findById(id);
    }
    
    @Override
    @NonNull
    public List<Order> findByUserId(@NonNull Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    @Override
    @NonNull
    public Order createOrderForUser(@NonNull User user, @NonNull List<OrderItem> items) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        
        // Set the order reference on each item for JPA cascading
        items.forEach(item -> item.setOrder(order));
        order.setItems(items);
        
        // Calculate total cost
        double totalCost = items.stream()
                .mapToDouble(OrderItem::getOrderItemPrice)
                .sum();
        order.setTotalCost(totalCost);
        
        return orderRepository.create(order);
    }
    
    // -------------------
    // DTO Operations
    // -------------------
    
    @Override
    @NonNull
    public List<OrderDTO> getAllOrdersDTO() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    
    @Override
    @NonNull
    public OrderDTO getOrderByIdDTO(@NonNull Long id) {
        return orderRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id: " + id
                ));
    }
    
    @Override
    @NonNull
    public List<OrderDTO> getOrdersByUserDTO(@NonNull Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    
    private OrderDTO mapToDTO(Order order) {
        UserDTO userDTO = new UserDTO(
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getUser().getEmail()
        );

        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        item.getQuantity(),
                        new ProductDTO(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getProduct().getDescription(),
                                item.getProduct().getPrice(),
                                item.getProduct().getDiscount(),
                                item.getProduct().getImageLink()
                        ),
                        item.getUnitPrice(),
                        item.getOrderItemPrice()
                ))
                .toList();

        return new OrderDTO(order.getId(), order.getOrderDate(), userDTO, items, order.getCouponApplied(), order.getTotalCost());
    }
}

