package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Orders, Long> {}
