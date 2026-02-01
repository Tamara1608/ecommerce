package com.example.ecommerce.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.Order.entity.Order;

import java.util.List;

public interface OrderTable extends JpaRepository<Order, Long> {
    
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE o.user.id = :userId")
    List<Order> findOrdersWithItemsAndProductsByUserId(@Param("userId") Long userId);
}

