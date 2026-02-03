package com.example.ecommerce.FlashSale;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.FlashSale.entity.FlashSaleEvent;

import java.util.List;
import java.util.Optional;


@Repository
@Qualifier("dbFlashSaleRepository")
@RequiredArgsConstructor
public class DatabaseFlashSaleRepository implements IFlashSaleRepository {
    
    private final FlashSaleTable flashSaleTable;
    
    // -------------------
    // CREATE operations
    // -------------------
    
    @Override
    @NonNull
    public FlashSaleEvent create(@NonNull FlashSaleEvent flashSale) {
        return flashSaleTable.save(flashSale);
    }
    
    // -------------------
    // READ operations
    // -------------------
    
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
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    @Override
    @NonNull
    public FlashSaleEvent update(@NonNull FlashSaleEvent flashSale) {
        return flashSaleTable.save(flashSale);
    }
    
    // -------------------
    // DELETE operations
    // -------------------
    
    @Override
    public void delete(@NonNull Long id) {
        flashSaleTable.deleteById(id);
    }
}

