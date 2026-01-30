package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock,Long> {
    
    // Find stocks by their IDs
    List<Stock> findByIdIn(List<Long> stockIds);
    
    @Query(value = "SELECT s.id, s.current_value, s.total_stock, s.updated_at FROM stocks s", nativeQuery = true)
    List<Object[]> findAllStocksRaw();
}
