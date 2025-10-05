package com.example.ecommerce.service;
import com.example.ecommerce.DTO.OrderDTO;
import com.example.ecommerce.DTO.OrderItemDTO;
import com.example.ecommerce.DTO.ProductDTO;
import com.example.ecommerce.DTO.UserDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    public OrderDTO getOrderById (Long id){
        return orderRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,  "Order not found!"
                ));
    }

    public List<OrderDTO> getOrdersByUser (Long id){
       return orderRepository.findOrdersWithItemsAndProductsByUserId(id)
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
                                item.getProduct().getTotalStock(),
                                item.getProduct().getPercentageOff(),
                                item.getProduct().getImageLink()
                        ),
                        item.getUnitPrice(),
                        item.getOrderItemPrice()
                ))
                .toList();

        return new OrderDTO(order.getId(), order.getOrderDate(), userDTO, items, order.getCouponApplied(),order.getDiscountAmount(),order.getTotalCost());
    }

}

