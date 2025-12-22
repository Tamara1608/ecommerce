package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockRepository extends JpaRepository<Stock,Long> {
    @Query("SELECT s FROM Stock s JOIN FETCH s.product WHERE s.product.id IN :productIds")
    List<Stock> findByProductIds(@Param("productIds") List<Long> productIds);
    
    @Query(value = "SELECT s.id, s.current_value, s.total_stock, s.product_id " +
           "FROM stocks s WHERE s.product_id IN :productIds", nativeQuery = true)
    List<Object[]> findByProductIdsRaw(@Param("productIds") List<Long> productIds);
    
   
    @Query(value = "SELECT s.id, s.current_value, s.total_stock, s.product_id FROM stocks s", nativeQuery = true)
    List<Object[]> findAllStocksRaw();
}
