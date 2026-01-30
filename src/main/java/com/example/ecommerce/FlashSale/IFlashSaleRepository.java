package com.example.ecommerce.FlashSale;

import com.example.ecommerce.entity.FlashSaleEvent;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FlashSaleEvent CRUD operations.
 * All methods are stateful and result in permanent changes.
 */
public interface IFlashSaleRepository {
    
    // -------------------
    // CREATE operations
    // -------------------
    
    /**
     * Create a new flash sale event.
     * Stateful operation - persists immediately.
     * 
     * @param flashSale the flash sale event to create
     * @return the created flash sale event with generated ID
     */
    @NonNull
    FlashSaleEvent create(@NonNull FlashSaleEvent flashSale);
    
    // -------------------
    // READ operations
    // -------------------
    
    /**
     * Find all flash sale events.
     * 
     * @return list of all flash sale events
     */
    @NonNull
    List<FlashSaleEvent> findAll();
    
    /**
     * Find a flash sale event by ID.
     * 
     * @param id the flash sale event ID
     * @return Optional containing the flash sale event if found, empty otherwise
     */
    @NonNull
    Optional<FlashSaleEvent> findById(@NonNull Long id);
    
    // -------------------
    // UPDATE operations
    // -------------------
    
    /**
     * Update an existing flash sale event.
     * Stateful operation - persists immediately.
     * 
     * @param flashSale the flash sale event to update
     * @return the updated flash sale event
     */
    @NonNull
    FlashSaleEvent update(@NonNull FlashSaleEvent flashSale);
    
    // -------------------
    // DELETE operations
    // -------------------
    
    /**
     * Delete a flash sale event by ID.
     * Stateful operation - deletes immediately.
     * 
     * @param id the flash sale event ID to delete
     */
    void delete(@NonNull Long id);
}

