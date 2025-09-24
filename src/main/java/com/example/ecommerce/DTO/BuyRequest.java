package com.example.ecommerce.DTO;

import lombok.Data;

import java.util.List;
@Data
public class BuyRequest {
    private Long userId;
    private List<ProductOrder> products;

    @Data
    public static class ProductOrder {
        private Long productId;
        private Integer quantity;
    }
}
