package com.example.ecommerce.Order;

import com.example.ecommerce.Product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Long id;
    private Integer quantity;
    private ProductDTO product;
    private Double unitPrice;
    private Double orderItemPrice;
}

