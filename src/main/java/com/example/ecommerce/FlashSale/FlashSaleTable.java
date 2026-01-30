package com.example.ecommerce.FlashSale;

import com.example.ecommerce.entity.FlashSaleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FlashSaleTable extends JpaRepository<FlashSaleEvent, Long> {
    
    @Query("""
        SELECT f
        FROM FlashSaleEvent f
        LEFT JOIN FETCH f.products
    """)
    List<FlashSaleEvent> findAllWithProducts();
}

