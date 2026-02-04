package com.example.ecommerce.product.infrastructure.persistence.stock;

import com.example.ecommerce.product.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockTable extends JpaRepository<Stock, Long> {
    
    @Modifying
    @Query("UPDATE Stock s SET s.currentValue = :currentValue WHERE s.id = :stockId")
    void updateCurrentValue(@Param("stockId") Long stockId, @Param("currentValue") Integer currentValue);
}

