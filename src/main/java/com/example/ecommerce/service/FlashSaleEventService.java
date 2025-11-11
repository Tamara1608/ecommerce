package com.example.ecommerce.service;

import com.example.ecommerce.entity.FlashSaleEvent;
import com.example.ecommerce.repository.FlashSaleEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FlashSaleEventService {

    private final FlashSaleEventRepository flashSaleEventRepository;

    public List<FlashSaleEvent> getAllFlashSales() {
        return flashSaleEventRepository.findAll();
    }

    public Optional<FlashSaleEvent> getFlashSaleById(Long id) {
        return flashSaleEventRepository.findById(id);
    }

    public FlashSaleEvent createFlashSale(FlashSaleEvent flashSaleEvent) {
        return flashSaleEventRepository.save(flashSaleEvent);
    }

    public FlashSaleEvent updateFlashSale(Long id, FlashSaleEvent updatedEvent) {
        return flashSaleEventRepository.findById(id)
                .map(existingEvent -> {
                    existingEvent.setName(updatedEvent.getName());
                    existingEvent.setStartDate(updatedEvent.getStartDate());
                    existingEvent.setEndDate(updatedEvent.getEndDate());
                    existingEvent.setProducts(updatedEvent.getProducts());
                    return flashSaleEventRepository.save(existingEvent);
                })
                .orElseThrow(() -> new RuntimeException("Flash Sale not found with id: " + id));
    }

    public void deleteFlashSale(Long id) {
        flashSaleEventRepository.deleteById(id);
    }
}

