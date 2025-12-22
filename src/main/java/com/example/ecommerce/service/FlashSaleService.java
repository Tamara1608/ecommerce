package com.example.ecommerce.service;
import com.example.ecommerce.DTO.BuyRequest;
import com.example.ecommerce.DTO.FlashSaleEventDTO;
import com.example.ecommerce.entity.FlashSaleEvent;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.FlashSaleEventRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlashSaleService {
    private final String STOCK_KEY_PREFIX = "stock:";
    private final String FLASH_SALE_PREFIX = "flashsale:event:";
    private final String ACTIVE_FLASH_SALES_KEY = "flashsales:active";
    private final String FLASH_SALE_PRODUCTS_PREFIX = "flashsale:products:";
    private final String PRODUCT_FLASH_SALE_PREFIX = "flashsale:product:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FlashSaleEventRepository flashSaleEventRepository;
    // private final CouponService couponService;


    public String buyProducts(BuyRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        StringBuilder result = new StringBuilder();

        for (BuyRequest.ProductOrder po : request.getProducts()) {
            String stockKey = STOCK_KEY_PREFIX + po.getProductId();

            Long stock = redisTemplate.opsForValue().decrement(stockKey, po.getQuantity());

            if (stock != null && stock >= 0) {
                Product product = productRepository.findById(po.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                OrderItem item = createOrderItem(order, product, po);

                items.add(item);

                result.append("Ordered ").append(po.getQuantity())
                        .append("x ").append(product.getName())
                        .append(". Remaining stock: ").append(stock).append("\n");

            } else { // if stock < 0 or null
                redisTemplate.opsForValue().increment(stockKey, po.getQuantity());
                result.append(productRepository.findById(po.getProductId())
                                .map(Product::getName).orElse("Unknown"))
                        .append(" is Sold Out!\n");
            }
        }
        // Should I involve coupons or not ?

        if (items.isEmpty()) {
            return "No products could be ordered. All sold out.";
        }
        order.setItems(items);
        orderRepository.save(order);

        return result.toString();
    }

    private OrderItem createOrderItem(Order o, Product p, BuyRequest.ProductOrder po){
        OrderItem item = new OrderItem();
        item.setOrder(o);
        item.setProduct(p);
        Integer quantity = po.getQuantity();
        item.setQuantity(quantity);
        double unitPrice = p.getPrice();
        item.setUnitPrice(unitPrice);
        item.setOrderItemPrice(unitPrice * quantity);

        return item;
    }

    // ====================
    // Flash Sale Event CRUD Operations
    // ====================

  
    public List<FlashSaleEvent> getAllFlashSales() {
        return flashSaleEventRepository.findAll();
    }

  
    public Optional<FlashSaleEvent> getFlashSaleById(Long id) {
        return flashSaleEventRepository.findById(id);
    }

   
    public FlashSaleEvent createFlashSale(FlashSaleEvent flashSaleEvent) {
        FlashSaleEvent saved = flashSaleEventRepository.save(flashSaleEvent);
        
        // Cache with TTL if it's active or will be active soon
        if (isActive(saved) || saved.getStartDate().isAfter(LocalDateTime.now())) {
            cacheFlashSale(saved);
            // Invalidate active flash sales list cache and rebuilds it 
            // on call getActiveFlashSales()
            redisTemplate.delete(ACTIVE_FLASH_SALES_KEY);
        }
        
        return saved;
    }

   
    public FlashSaleEvent updateFlashSale(Long id, FlashSaleEvent updatedEvent) {
        FlashSaleEvent updated = flashSaleEventRepository.findById(id)
                .map(existingEvent -> {
                    existingEvent.setName(updatedEvent.getName());
                    existingEvent.setStartDate(updatedEvent.getStartDate());
                    existingEvent.setEndDate(updatedEvent.getEndDate());
                    existingEvent.setProducts(updatedEvent.getProducts());
                    return flashSaleEventRepository.save(existingEvent);
                })
                .orElseThrow(() -> new RuntimeException("Flash Sale not found with id: " + id));
        
        updateFlashSaleCache(updated);
        
        return updated;
    }

    
    public void deleteFlashSale(Long id) {
        flashSaleEventRepository.deleteById(id);
        invalidateFlashSaleCache(id);
    }

    // ====================
    // TTL-Based Flash Sale Caching
    // ====================

 
    public void cacheFlashSale(FlashSaleEvent event) {
        String key = FLASH_SALE_PREFIX + event.getId();
        LocalDateTime now = LocalDateTime.now();
        
        // Calculate TTL until endDate
        if (event.getEndDate().isAfter(now)) {
            Duration ttl = Duration.between(now, event.getEndDate());
            if (ttl.getSeconds() > 0) {
                // Convert to DTO (only product IDs, not full entities)
                FlashSaleEventDTO dto = convertToDTO(event);
                redisTemplate.opsForValue().set(key, dto, ttl);
                
                cacheFlashSaleProducts(event);
            }
        }
    }


    private void cacheFlashSaleProducts(FlashSaleEvent event) {
        String key = FLASH_SALE_PRODUCTS_PREFIX + event.getId();
        LocalDateTime now = LocalDateTime.now();
        
        if (event.getEndDate().isAfter(now)) {
         
            Set<Long> productIds = event.getProducts().stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            
            Duration ttl = Duration.between(now, event.getEndDate());
            if (ttl.getSeconds() > 0) {
                redisTemplate.opsForValue().set(key, productIds, ttl);
                
                // Cache reverse mapping: product -> flash sale
                for (Long productId : productIds) {
                    String productKey = PRODUCT_FLASH_SALE_PREFIX + productId;
                    redisTemplate.opsForValue().set(productKey, event.getId(), ttl);
                }
            }
        }
    }

 
    private void updateFlashSaleCache(FlashSaleEvent event) {
        LocalDateTime now = LocalDateTime.now();
        
        // Only update if event is active or will be active
        if (event.getEndDate().isAfter(now)) {
            String key = FLASH_SALE_PREFIX + event.getId();
            
            // Calculate new TTL based on updated endDate
            Duration newTTL = Duration.between(now, event.getEndDate());
            
            if (newTTL.getSeconds() > 0) {
                // Update cache directly with new TTL (overwrites existing value)
                // Convert to DTO to avoid caching full Product entities
                FlashSaleEventDTO dto = convertToDTO(event);
                redisTemplate.opsForValue().set(key, dto, newTTL);
                
                // Update product mappings (handles product changes)
                updateFlashSaleProductMappings(event, newTTL);
                
                // Invalidate active list (it needs to be rebuilt with updated data)
                redisTemplate.delete(ACTIVE_FLASH_SALES_KEY);
            } else {
                // If TTL expired, delete cache
                invalidateFlashSaleCache(event.getId());
            }
        } else {
            // Event ended, remove from cache
            invalidateFlashSaleCache(event.getId());
        }
    }

 
    private void updateFlashSaleProductMappings(FlashSaleEvent event, Duration ttl) {
        String productsKey = FLASH_SALE_PRODUCTS_PREFIX + event.getId();
        
        // Get old product IDs from cache (if exists)
        Set<Long> oldProductIds = new HashSet<>();
        Object oldProducts = redisTemplate.opsForValue().get(productsKey);
        if (oldProducts instanceof Set) {
            @SuppressWarnings("unchecked")
            Set<Long> old = (Set<Long>) oldProducts;
            oldProductIds.addAll(old);
        }
        
        // Get new product IDs
        Set<Long> newProductIds = event.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
        
        // Remove mappings for products that are no longer in flash sale
        for (Long oldProductId : oldProductIds) {
            if (!newProductIds.contains(oldProductId)) {
                String productKey = PRODUCT_FLASH_SALE_PREFIX + oldProductId;
                redisTemplate.delete(productKey);
            }
        }
        
        // Update/add mappings for new products
        redisTemplate.opsForValue().set(productsKey, newProductIds, ttl);
        for (Long productId : newProductIds) {
            String productKey = PRODUCT_FLASH_SALE_PREFIX + productId;
            redisTemplate.opsForValue().set(productKey, event.getId(), ttl);
        }
    }

  
    public Optional<FlashSaleEvent> getActiveFlashSale(Long id) {
        String key = FLASH_SALE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);
        
        if (cached instanceof FlashSaleEventDTO dto) {
            // Convert DTO back to entity and check if still active
            FlashSaleEvent event = convertToEntity(dto);
            if (isActive(event)) {
                return Optional.of(event);
            }
        }
        
        // Fallback to DB
        return flashSaleEventRepository.findById(id)
                .filter(this::isActive)
                .map(event -> {
                    cacheFlashSale(event);
                    return event;
                });
    }

    /**
     * Get all active flash sales (cached with TTL)
     */
    public List<FlashSaleEvent> getActiveFlashSales() {
        // Check cache first
        Object cached = redisTemplate.opsForValue().get(ACTIVE_FLASH_SALES_KEY);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<FlashSaleEventDTO> dtos = (List<FlashSaleEventDTO>) cached;
            // Convert DTOs to entities and filter to ensure they're still active
            return dtos.stream()
                    .map(this::convertToEntity)
                    .filter(this::isActive)
                    .toList();
        }
        
        // Load from DB and cache
        List<FlashSaleEvent> allEvents = flashSaleEventRepository.findAll();
        List<FlashSaleEvent> activeEvents = allEvents.stream()
                .filter(this::isActive)
                .toList();
        
        if (!activeEvents.isEmpty()) {
            // Cache with TTL = shortest endDate among active events
            LocalDateTime now = LocalDateTime.now();
            Optional<Duration> shortestTTL = activeEvents.stream()
                    .map(FlashSaleEvent::getEndDate)
                    .filter(end -> end.isAfter(now))
                    .map(end -> Duration.between(now, end))
                    .min(Duration::compareTo);
            
            if (shortestTTL.isPresent() && shortestTTL.get().getSeconds() > 0) {
                // Cache as DTOs (not full entities)
                List<FlashSaleEventDTO> dtos = activeEvents.stream()
                        .map(this::convertToDTO)
                        .toList();
                redisTemplate.opsForValue().set(ACTIVE_FLASH_SALES_KEY, dtos, shortestTTL.get());
            }
            
            // Cache individual flash sales
            activeEvents.forEach(this::cacheFlashSale);
        }
        
        return activeEvents;
    }

    public boolean isProductInActiveFlashSale(Long productId) {
        // Check reverse mapping cache first (fastest - O(1) lookup)
        String productKey = PRODUCT_FLASH_SALE_PREFIX + productId;
        Object flashSaleId = redisTemplate.opsForValue().get(productKey);
        
        if (flashSaleId instanceof Long id) {
            // Verify the flash sale is still active by checking the DTO (no entity conversion needed)
            String flashSaleKey = FLASH_SALE_PREFIX + id;
            Object cached = redisTemplate.opsForValue().get(flashSaleKey);
            if (cached instanceof FlashSaleEventDTO dto) {
                if (isActive(dto) && dto.getProductIds().contains(productId)) {
                    return true;
                }
            }
        }
        
        // Fallback: check all active flash sales
        List<FlashSaleEvent> activeSales = getActiveFlashSales();
        return activeSales.stream()
                .anyMatch(sale -> sale.getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId)));
    }

    /**
     * Get flash sale event for a specific product (if in active flash sale)
     */
    public Optional<FlashSaleEvent> getFlashSaleForProduct(Long productId) {
        // Check reverse mapping cache (fastest - O(1) lookup)
        String productKey = PRODUCT_FLASH_SALE_PREFIX + productId;
        Object flashSaleId = redisTemplate.opsForValue().get(productKey);
        
        if (flashSaleId instanceof Long id) {
            // Get flash sale from cache and verify it's active and contains the product
            String flashSaleKey = FLASH_SALE_PREFIX + id;
            Object cached = redisTemplate.opsForValue().get(flashSaleKey);
            if (cached instanceof FlashSaleEventDTO dto) {
                // Check if active and contains product (using DTO directly - more efficient)
                if (isActive(dto) && dto.getProductIds().contains(productId)) {
                    // Convert to entity only when needed (for return value)
                    return Optional.of(convertToEntity(dto));
                }
            }
        }
        
        // Fallback: search all active flash sales
        return getActiveFlashSales().stream()
                .filter(sale -> sale.getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId)))
                .findFirst();
    }

    //Is there any active flash sale?
    public boolean hasActiveFlashSale() {
        // Quick check: if active flash sales cache exists, there's at least one active
        Object cached = redisTemplate.opsForValue().get(ACTIVE_FLASH_SALES_KEY);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<FlashSaleEventDTO> dtos = (List<FlashSaleEventDTO>) cached;
            return dtos.stream()
                    .map(this::convertToEntity)
                    .anyMatch(this::isActive);
        }
        
        // Fallback to DB check
        return !getActiveFlashSales().isEmpty();
    }

    // Check if flash sale is currently active (between startDate and endDate)
    private boolean isActive(FlashSaleEvent event) {
        LocalDateTime now = LocalDateTime.now();
        return event.getStartDate().isBefore(now) && event.getEndDate().isAfter(now);
    }

    // Check if flash sale DTO is currently active (between startDate and endDate)
    private boolean isActive(FlashSaleEventDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        return dto.getStartDate().isBefore(now) && dto.getEndDate().isAfter(now);
    }

    public void invalidateFlashSaleCache(Long flashSaleId) {
        redisTemplate.delete(FLASH_SALE_PREFIX + flashSaleId);
        redisTemplate.delete(FLASH_SALE_PRODUCTS_PREFIX + flashSaleId);
        redisTemplate.delete(ACTIVE_FLASH_SALES_KEY);
        
    }

    // ====================
    // Helper Methods: DTO Conversion
    // ====================

    private FlashSaleEventDTO convertToDTO(FlashSaleEvent event) {
        Set<Long> productIds = event.getProducts().stream()
                .map(Product::getId)
                .collect(Collectors.toSet());
        
        return new FlashSaleEventDTO(
                event.getId(),
                event.getName(),
                event.getStartDate(),
                event.getEndDate(),
                productIds
        );
    }


    private FlashSaleEvent convertToEntity(FlashSaleEventDTO dto) {
        FlashSaleEvent event = new FlashSaleEvent();
        event.setId(dto.getId());
        event.setName(dto.getName());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        
        // Fetch Product entities from repository using IDs
        Set<Product> products = dto.getProductIds().stream()
                .map(productRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        
        event.setProducts(products);
        return event;
    }

}

