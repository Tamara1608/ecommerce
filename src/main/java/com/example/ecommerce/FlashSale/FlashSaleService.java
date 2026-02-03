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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


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
    public Map<String, Object> create(@NonNull FlashSaleEvent flashSale) {
        List<Long> skipped = validateAndFilterProducts(flashSale);
        FlashSaleEvent created = flashSaleRepository.create(flashSale);
        
        String message = skipped.isEmpty() 
            ? "Flash sale created successfully" 
            : "Flash sale created, but " + skipped.size() + " product(s) were skipped (not found)";
        
        return Map.of(
            "flashSale", toDTO(created),
            "skippedProductIds", skipped,
            "message", message
        );
    }
    
    @NonNull
    public List<FlashSaleResponseDTO> findAll() {
        return flashSaleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @NonNull
    public FlashSaleResponseDTO findById(@NonNull Long id) {
        FlashSaleEvent event = flashSaleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + id
                ));
        return toDTO(event);
    }
    
    @NonNull
    public Map<String, Object> update(@NonNull FlashSaleEvent flashSale) {
        if (flashSale.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flash sale ID is required for update");
        }
        flashSaleRepository.findById(flashSale.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flash sale event not found with id: " + flashSale.getId()
                ));
        
        List<Long> skipped = validateAndFilterProducts(flashSale);
        FlashSaleEvent updated = flashSaleRepository.update(flashSale);
        
        String message = skipped.isEmpty() 
            ? "Flash sale updated successfully" 
            : "Flash sale updated, but " + skipped.size() + " product(s) were skipped (not found)";
        
        return Map.of(
            "flashSale", toDTO(updated),
            "skippedProductIds", skipped,
            "message", message
        );
    }
    

    private List<Long> validateAndFilterProducts(FlashSaleEvent flashSale) {
        if (flashSale.getProducts() == null || flashSale.getProducts().isEmpty()) {
            return List.of();
        }
        
        Set<Long> requestedIds = flashSale.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
        
        Set<Product> validProducts = new HashSet<>();
        List<Long> skippedIds = new ArrayList<>();
        
        for (Long productId : requestedIds) {
            try {
                Product product = productService.findById(productId);
                validProducts.add(product);
            } catch (ResponseStatusException e) {
                // Product not found - skip it
                skippedIds.add(productId);
            }
        }
        
        flashSale.setProducts(validProducts);
        return skippedIds;
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
    public List<FlashSaleResponseDTO> getActiveFlashSales() {
        return flashSaleRepository.findAll().stream()
                .filter(this::isActive)
                .map(this::toDTO)
                .toList();
    }
    

    @NonNull
    public Optional<FlashSaleResponseDTO> getActiveFlashSale(@NonNull Long id) {
        return flashSaleRepository.findById(id)
                .filter(this::isActive)
                .map(this::toDTO);
    }
    
    public boolean isProductInActiveFlashSale(@NonNull Long productId) {
        return flashSaleRepository.findAll().stream()
                .filter(this::isActive)
                .anyMatch(sale -> sale.getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId)));
    }
    
    @NonNull
    public Optional<FlashSaleResponseDTO> getFlashSaleForProduct(@NonNull Long productId) {
        return flashSaleRepository.findAll().stream()
                .filter(this::isActive)
                .filter(sale -> sale.getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId)))
                .findFirst()
                .map(this::toDTO);
    }
    
    /**
     * Check if there are any active flash sales.
     */
    public boolean hasActiveFlashSale() {
        return flashSaleRepository.findAll().stream()
                .anyMatch(this::isActive);
    }
    
    /**
     * Check if a flash sale is currently active (between startDate and endDate).
     */
    public boolean isActive(@NonNull FlashSaleEvent event) {
        LocalDateTime now = LocalDateTime.now();
        return event.getStartDate().isBefore(now) && event.getEndDate().isAfter(now);
    }
    
    // -------------------
    // DTO Conversion
    // -------------------
    
    private FlashSaleResponseDTO toDTO(FlashSaleEvent event) {
        List<ProductBasicDTO> productDTOs = event.getProducts().stream()
                .map(this::toProductDTO)
                .collect(Collectors.toList());
        
        return new FlashSaleResponseDTO(
                event.getId(),
                event.getName(),
                event.getStartDate(),
                event.getEndDate(),
                productDTOs
        );
    }
    
    private ProductBasicDTO toProductDTO(Product product) {
        return new ProductBasicDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscount(),
                product.getImageLink()
        );
    }
}

