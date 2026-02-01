package com.example.ecommerce.FlashSale;

import com.example.ecommerce.FlashSale.BuyRequest;
import com.example.ecommerce.FlashSale.entity.FlashSaleEvent;
import com.example.ecommerce.Order.IOrderService;
import com.example.ecommerce.Order.entity.OrderItem;
import com.example.ecommerce.Product.IProductService;
import com.example.ecommerce.Product.entity.Product;
import com.example.ecommerce.User.entity.User;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for FlashSaleEvent CRUD operations and flash sale purchases.
 * Repository implementation is injected - can be cached or non-cached.
 */
public class FlashSaleService {
    
    private final IFlashSaleRepository flashSaleRepository;
    private final IProductService productService;
    private final IOrderService orderService;
    
    public FlashSaleService(
            IFlashSaleRepository flashSaleRepository,
            IProductService productService,
            IOrderService orderService) {
        this.flashSaleRepository = flashSaleRepository;
        this.productService = productService;
        this.orderService = orderService;
    }
    
    // -------------------
    // Flash Sale Purchase Operations
    // -------------------
    
    /**
     * Buy products during a flash sale.
     * Checks stock via productService, creates order items, and saves the order.
     * Throws exception if any product is out of stock.
     * 
     * @param user the user making the purchase
     * @param productOrders list of product orders with productId and quantity
     * @throws ResponseStatusException if any product is out of stock
     */
    public void buyProducts(@NonNull User user, @NonNull List<BuyRequest.ProductOrder> productOrders) {
        List<OrderItem> items = new ArrayList<>();
        
        for (BuyRequest.ProductOrder po : productOrders) {
            Product product = productService.returnIfInStock(po.getProductId(), po.getQuantity())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Product " + po.getProductId() + " is out of stock"
                    ));
            
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(po.getQuantity());
            item.setUnitPrice(product.getPrice());
            item.setOrderItemPrice(product.getPrice() * po.getQuantity());
            items.add(item);
        }
        
        // createOrderForUser will create the Order and set the relationship on items
        orderService.createOrderForUser(user, items);
    }
    
    // -------------------
    // Flash Sale CRUD Operations
    // -------------------
    
    @NonNull
    public FlashSaleEvent create(@NonNull FlashSaleEvent flashSale) {
        return flashSaleRepository.create(flashSale);
    }
    
    @NonNull
    public List<FlashSaleEvent> findAll() {
        return flashSaleRepository.findAll();
    }
    
    @NonNull
    public FlashSaleEvent findById(@NonNull Long id) {
        return flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + id
                ));
    }
    
    @NonNull
    public FlashSaleEvent update(@NonNull FlashSaleEvent flashSale) {
        if (flashSale.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flash sale ID is required for update");
        }
        flashSaleRepository.findById(flashSale.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + flashSale.getId()
                ));
        return flashSaleRepository.update(flashSale);
    }
    
    public void delete(@NonNull Long id) {
        flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + id
                ));
        flashSaleRepository.delete(id);
    }
    
    // -------------------
    // Active Flash Sale Operations
    // -------------------
    
    /**
     * Get all active flash sales (currently running).
     */
    @NonNull
    public List<FlashSaleEvent> getActiveFlashSales() {
        return flashSaleRepository.findAll().stream()
                .filter(this::isActive)
                .toList();
    }
    
    /**
     * Get an active flash sale by ID.
     */
    @NonNull
    public Optional<FlashSaleEvent> getActiveFlashSale(@NonNull Long id) {
        return flashSaleRepository.findById(id)
                .filter(this::isActive);
    }
    
    /**
     * Check if a product is in any active flash sale.
     */
    public boolean isProductInActiveFlashSale(@NonNull Long productId) {
        return getActiveFlashSales().stream()
                .anyMatch(sale -> sale.getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId)));
    }
    
    /**
     * Get the flash sale event for a specific product (if in active flash sale).
     */
    @NonNull
    public Optional<FlashSaleEvent> getFlashSaleForProduct(@NonNull Long productId) {
        return getActiveFlashSales().stream()
                .filter(sale -> sale.getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId)))
                .findFirst();
    }
    
    /**
     * Check if there are any active flash sales.
     */
    public boolean hasActiveFlashSale() {
        return !getActiveFlashSales().isEmpty();
    }
    
    /**
     * Check if a flash sale is currently active (between startDate and endDate).
     */
    public boolean isActive(@NonNull FlashSaleEvent event) {
        LocalDateTime now = LocalDateTime.now();
        return event.getStartDate().isBefore(now) && event.getEndDate().isAfter(now);
    }
}

