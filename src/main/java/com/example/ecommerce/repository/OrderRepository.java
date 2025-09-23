package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
//    List<Order> findByUserId(Long userId);


    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items i " +
            "LEFT JOIN FETCH i.product " +
            "WHERE o.user.id = :userId")
    List<Order> findOrdersWithItemsAndProductsByUserId(@Param("userId") Long userId);

}
