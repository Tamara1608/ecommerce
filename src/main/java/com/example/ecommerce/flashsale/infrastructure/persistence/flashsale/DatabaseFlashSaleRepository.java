package com.example.ecommerce.flashsale.infrastructure.persistence.flashsale;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.flashsale.domain.FlashSaleEvent;

import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("dbFlashSaleRepository")
@RequiredArgsConstructor
public class DatabaseFlashSaleRepository implements IFlashSaleRepository {
    
    private final FlashSaleTable flashSaleTable;
    
    @Override
    @NonNull
    public FlashSaleEvent create(@NonNull FlashSaleEvent flashSale) {
        return flashSaleTable.save(flashSale);
    }
    
    @Override
    @NonNull
    public List<FlashSaleEvent> findAll() {
        return flashSaleTable.findAll();
    }
    
    @Override
    @NonNull
    public Optional<FlashSaleEvent> findById(@NonNull Long id) {
        return flashSaleTable.findById(id);
    }
    
    @Override
    @NonNull
    public FlashSaleEvent update(@NonNull FlashSaleEvent flashSale) {
        return flashSaleTable.save(flashSale);
    }
    
    @Override
    public void delete(@NonNull Long id) {
        flashSaleTable.deleteById(id);
    }
}

