package com.example.ecommerce.flashsale.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductBasicDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer discount;
    private String imageLink;
}

