package com.example.ecommerce.repository;

import com.example.ecommerce.entity.FlashSaleEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashSaleEventRepository extends JpaRepository<FlashSaleEvent, Long>{

}
