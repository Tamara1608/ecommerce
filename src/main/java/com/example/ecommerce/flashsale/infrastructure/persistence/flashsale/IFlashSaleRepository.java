package com.example.ecommerce.flashsale.infrastructure.persistence.flashsale;

import org.springframework.lang.NonNull;

import com.example.ecommerce.flashsale.domain.FlashSaleEvent;

import java.util.List;
import java.util.Optional;


public interface IFlashSaleRepository {
    

    @NonNull
    FlashSaleEvent create(@NonNull FlashSaleEvent flashSale);

    @NonNull
    List<FlashSaleEvent> findAll();
    
    @NonNull
    Optional<FlashSaleEvent> findById(@NonNull Long id);

    @NonNull
    FlashSaleEvent update(@NonNull FlashSaleEvent flashSale);
    
    void delete(@NonNull Long id);
}

