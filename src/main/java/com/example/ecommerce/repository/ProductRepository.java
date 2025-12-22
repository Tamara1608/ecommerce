package com.example.ecommerce.repository;
import com.example.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
 
    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.stock
    """)
    List<Product> findAllWithStock();
   
}
