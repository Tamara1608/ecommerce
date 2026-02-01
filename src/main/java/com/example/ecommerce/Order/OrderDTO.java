package com.example.ecommerce.Order;

import com.example.ecommerce.User.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private LocalDateTime orderDate;
    private UserDTO user;
    private List<OrderItemDTO> items;
    private String couponApplied;
    private Double totalCost;
}

