package com.example.ecommerce.flashsale.infrastructure.persistence.flashsale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ecommerce.flashsale.domain.FlashSaleEvent;

import java.util.List;

public interface FlashSaleTable extends JpaRepository<FlashSaleEvent, Long> {
    
    @Query("""
        SELECT DISTINCT f
        FROM FlashSaleEvent f
        LEFT JOIN FETCH f.products
    """)
    List<FlashSaleEvent> findAllWithProducts();
}

