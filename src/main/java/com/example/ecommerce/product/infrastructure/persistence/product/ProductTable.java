package com.example.ecommerce.product.infrastructure.persistence.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.product.domain.Product;

import java.util.List;
import java.util.Optional;

public interface ProductTable extends JpaRepository<Product, Long> {
 
    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN FETCH p.stock
        LEFT JOIN FETCH p.categories
        LEFT JOIN FETCH p.priceHistory
    """)
    List<Product> findAllWithStock();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.stock
        WHERE p.id = :id
    """)
    Optional<Product> findByIdWithStock(@Param("id") Long id);
   
}
