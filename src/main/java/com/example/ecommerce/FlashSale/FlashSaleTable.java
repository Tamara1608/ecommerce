package com.example.ecommerce.FlashSale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ecommerce.FlashSale.entity.FlashSaleEvent;

import java.util.List;

public interface FlashSaleTable extends JpaRepository<FlashSaleEvent, Long> {
    
    @Query("""
        SELECT DISTINCT f
        FROM FlashSaleEvent f
        LEFT JOIN FETCH f.products
    """)
    List<FlashSaleEvent> findAllWithProducts();
}

