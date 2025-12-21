package com.example.ecommerce.service;
import com.example.ecommerce.DTO.BuyRequest;
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

    /**
     * Get all flash sales (not filtered by active status)
     */
    public List<FlashSaleEvent> getAllFlashSales() {
        return flashSaleEventRepository.findAll();
    }

    /**
     * Get flash sale by ID (returns any flash sale, not just active)
     */
    public Optional<FlashSaleEvent> getFlashSaleById(Long id) {
        return flashSaleEventRepository.findById(id);
    }

    /**
     * Create flash sale event and cache it with TTL
     */
    public FlashSaleEvent createFlashSale(FlashSaleEvent flashSaleEvent) {
        FlashSaleEvent saved = flashSaleEventRepository.save(flashSaleEvent);
        
        // Cache with TTL if it's active or will be active soon
        if (isActive(saved) || saved.getStartDate().isAfter(LocalDateTime.now())) {
            cacheFlashSale(saved);
            // Invalidate active flash sales list cache
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
                redisTemplate.opsForValue().set(key, event, ttl);
                
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
                redisTemplate.opsForValue().set(key, event, newTTL);
                
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

    /**
     * Update product-to-flash-sale mappings when flash sale is updated
     * Handles product additions and removals
     */
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

    /**
     * Get active flash sale from cache or DB
     * Returns Optional.empty() if flash sale is not active
     */
    public Optional<FlashSaleEvent> getActiveFlashSale(Long id) {
        String key = FLASH_SALE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);
        
        if (cached instanceof FlashSaleEvent event) {
            // Double-check it's still active (cache might not have expired yet)
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
            List<FlashSaleEvent> events = (List<FlashSaleEvent>) cached;
            // Filter to ensure they're still active (cache might not have expired)
            return events.stream()
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
                redisTemplate.opsForValue().set(ACTIVE_FLASH_SALES_KEY, activeEvents, shortestTTL.get());
            }
            
            // Cache individual flash sales
            activeEvents.forEach(this::cacheFlashSale);
        }
        
        return activeEvents;
    }

    /**
     * Check if a product is in any active flash sale
     */
    public boolean isProductInActiveFlashSale(Long productId) {
        // Check reverse mapping cache first
        String productKey = PRODUCT_FLASH_SALE_PREFIX + productId;
        Object flashSaleId = redisTemplate.opsForValue().get(productKey);
        
        if (flashSaleId instanceof Long id) {
            Optional<FlashSaleEvent> flashSale = getActiveFlashSale(id);
            if (flashSale.isPresent()) {
                return flashSale.get().getProducts().stream()
                        .anyMatch(p -> p.getId().equals(productId));
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
        // Check reverse mapping cache
        String productKey = PRODUCT_FLASH_SALE_PREFIX + productId;
        Object flashSaleId = redisTemplate.opsForValue().get(productKey);
        
        if (flashSaleId instanceof Long id) {
            Optional<FlashSaleEvent> flashSale = getActiveFlashSale(id);
            if (flashSale.isPresent() && flashSale.get().getProducts().stream()
                    .anyMatch(p -> p.getId().equals(productId))) {
                return flashSale;
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
            List<FlashSaleEvent> events = (List<FlashSaleEvent>) cached;
            return events.stream().anyMatch(this::isActive);
        }
        
        // Fallback to DB check
        return !getActiveFlashSales().isEmpty();
    }

    // Check if flash sale is currently active (between startDate and endDate)
    private boolean isActive(FlashSaleEvent event) {
        LocalDateTime now = LocalDateTime.now();
        return event.getStartDate().isBefore(now) && event.getEndDate().isAfter(now);
    }

    public void invalidateFlashSaleCache(Long flashSaleId) {
        redisTemplate.delete(FLASH_SALE_PREFIX + flashSaleId);
        redisTemplate.delete(FLASH_SALE_PRODUCTS_PREFIX + flashSaleId);
        redisTemplate.delete(ACTIVE_FLASH_SALES_KEY);
        
    }

}

