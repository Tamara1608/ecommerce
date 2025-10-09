package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer totalStock; // null initially

    private Integer currentValue; // kept in redis

    private LocalDateTime updatedAt; // only updated when total stock is set!

    @ManyToOne(fetch = FetchType.LAZY) // I only fetch the product here when call - stock.getProduct();
    @JoinColumn(name = "product_id")
    private Product product;
}
