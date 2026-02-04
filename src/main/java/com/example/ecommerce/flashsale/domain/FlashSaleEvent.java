package com.example.ecommerce.flashsale.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.example.ecommerce.product.domain.Product;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table
public class FlashSaleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @ManyToMany
    @JoinTable(
            name = "flash_sale_products",
            joinColumns = @JoinColumn(name = "flash_sale_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

}
