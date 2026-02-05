package com.example.ecommerce.flashsale.infrastructure.persistence.flashsale;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.flashsale.domain.FlashSaleEvent;

import java.util.List;

public interface FlashSaleTable extends JpaRepository<FlashSaleEvent, Long> {
    
    @Query("""
        SELECT DISTINCT f
        FROM FlashSaleEvent f
        LEFT JOIN FETCH f.products
    """)
    List<FlashSaleEvent> findAllWithProducts();
    
    /**
     * Remove a product from all flash sale events.
     * This must be called before deleting a product to avoid FK constraint violations.
     */
    @Modifying
    @Query(value = "DELETE FROM flash_sale_products WHERE product_id = :productId", nativeQuery = true)
    void removeProductFromAllFlashSales(@Param("productId") Long productId);
}

