package com.example.ecommerce.service;
import com.example.ecommerce.DTO.OrderDTO;
import com.example.ecommerce.DTO.OrderItemDTO;
import com.example.ecommerce.DTO.ProductDTO;
import com.example.ecommerce.DTO.UserDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    public Order getOrderById (Long id){
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,  "Order not found!"
                ));
    }
    public List<Order> getOrdersByUser (Long id){
       return orderRepository.findOrdersWithItemsAndProductsByUserId(id);
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
                                item.getProduct().getPrice()
                        )
                ))
                .toList();

        return new OrderDTO(order.getId(), order.getOrderDate(), userDTO, items);
    }

}

