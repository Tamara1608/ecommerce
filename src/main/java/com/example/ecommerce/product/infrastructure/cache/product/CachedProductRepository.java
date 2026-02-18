package com.example.ecommerce.product.infrastructure.cache.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.flashsale.infrastructure.persistence.flashsale.FlashSaleTable;
import com.example.ecommerce.product.api.dto.ProductDTO;
import com.example.ecommerce.product.domain.Product;
import com.example.ecommerce.product.infrastructure.persistence.product.IProductRepository;
import com.example.ecommerce.product.infrastructure.persistence.product.ProductTable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@Qualifier("cachedProductRepository")
@RequiredArgsConstructor
@Slf4j
public class CachedProductRepository implements IProductRepository {
    
    private final ProductTable productTable;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FlashSaleTable flashSaleTable;
    
    private static final String CACHE_KEY_PREFIX = "product:";
    private static final String ALL_PRODUCTS_KEY = "products:all";
    private static final String STOCK_KEY_PREFIX = "stock:";
    
    private static final String ATOMIC_DECREMENT_SCRIPT = 
        "local stock = tonumber(redis.call('GET', KEYS[1]) or '0') " +
        "local quantity = tonumber(ARGV[1]) " +
        "if stock >= quantity then " +
        "    redis.call('DECRBY', KEYS[1], quantity) " +
        "    return 1 " +
        "else " +
        "    return 0 " +
        "end";
    

    @Override
    @NonNull
    public Product create(@NonNull Product product) {
        // Step 1: Write to database first
        Product saved = productTable.save(product);
        
        // Step 2: Write to cache (write-through)
        cacheProduct(saved);
        
        // Step 3: Invalidate all-products cache for consistency
        invalidateAllProductsCache();
        
        return saved;
    }
    
    @Override
    @NonNull
    public List<Product> findAll() {
        List<Product> products = productTable.findAllWithStock();
        products.forEach(this::cacheProduct);
        return products;
    }
    
    /**
     * Read-through caching for findAll:
     * 1. Check cache first
     * 2. If cache miss, fetch from DB
     * 3. Populate cache with the result
     * 4. Return data
     */
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public List<ProductDTO> findAllDTO() {
        // Step 1: Check cache
        try {
            List<ProductDTO> cachedDTOs = (List<ProductDTO>) redisTemplate.opsForValue().get(ALL_PRODUCTS_KEY);
            
            if (cachedDTOs != null) {
                return cachedDTOs;
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize cached products list, invalidating cache: {}", e.getMessage());
            invalidateAllProductsCache();
        }
        
        // Step 2: Cache miss - fetch from database
        List<Product> products = productTable.findAllWithStock();
        
        // Step 3: Populate cache (read-through)
        if (!products.isEmpty()) {
            List<ProductDTO> dtos = products.stream()
                    .map(this::productToDTO)
                    .collect(Collectors.toList());
            redisTemplate.opsForValue().set(ALL_PRODUCTS_KEY, dtos);
            
            products.forEach(this::cacheProduct);
        }
        
        List<ProductDTO> dtos = products.stream()
                .map(this::productToDTO)
                .collect(Collectors.toList());
        
        return dtos;
    }
    
    @Override
    @NonNull
    public Optional<Product> findById(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                ProductDTO dto = (ProductDTO) cached;
                return Optional.of(dtoToProduct(dto));
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize cached product {}, evicting from cache: {}", id, e.getMessage());
            evictFromCache(id);
        }
        
        Optional<Product> productOpt = productTable.findByIdWithAllRelations(id);
        
        productOpt.ifPresent(this::cacheProduct);
        
        return productOpt;
    }
    
    @Override
    @NonNull
    public Optional<ProductDTO> findByIdDTO(@NonNull Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                ProductDTO dto = (ProductDTO) cached;
                return Optional.of(dto);
            }
        } catch (Exception e) {
            log.warn("Failed to deserialize cached product {}, evicting from cache: {}", id, e.getMessage());
            evictFromCache(id);
        }
        
        Optional<Product> productOpt = productTable.findByIdWithAllRelations(id);
        
        productOpt.ifPresent(this::cacheProduct);
        
        return productOpt.map(this::productToDTO);
    }

    @Override
    @NonNull
    public Product update(@NonNull Product product) {
        // Step 1: Write to database first
        Product updated = productTable.save(product);
        
        // Step 2: Fetch with all relations to ensure fully loaded
        Product fullyLoaded = productTable.findByIdWithAllRelations(updated.getId())
                .orElse(updated);
        
        // Step 3: Update cache (write-through)
        cacheProduct(fullyLoaded);
        
        // Step 4: Invalidate all-products cache for consistency
        invalidateAllProductsCache();
        
        return fullyLoaded;
    }
    
    @Override
    @Transactional
    public void delete(@NonNull Long id) {
        // Remove product from all flash sales first to avoid FK constraint violation
        flashSaleTable.removeProductFromAllFlashSales(id);
        productTable.deleteById(id);
        evictFromCache(id);
        invalidateAllProductsCache();
    }
    
    @Override
    @NonNull
    public Optional<Product> returnIfInStock(@NonNull Long productId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(ATOMIC_DECREMENT_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(stockKey), quantity);
        
        if (result != null && result == 1) {
            String cacheKey = CACHE_KEY_PREFIX + productId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            
            if (cached != null) {
                ProductDTO dto = (ProductDTO) cached;
                return Optional.of(dtoToProduct(dto));
            }
            
            Optional<Product> productOpt = productTable.findById(productId);
            productOpt.ifPresent(this::cacheProduct);
            
            return productOpt;
        }
        
        return Optional.empty();
    }
        
    private void cacheProduct(Product product) {
        String cacheKey = CACHE_KEY_PREFIX + product.getId();
        ProductDTO dto = productToDTO(product);
        redisTemplate.opsForValue().set(cacheKey, dto);
    }
    
    private void evictFromCache(Long id) {
        String cacheKey = CACHE_KEY_PREFIX + id;
        redisTemplate.delete(cacheKey);
    }
    
    private void invalidateAllProductsCache() {
        redisTemplate.delete(ALL_PRODUCTS_KEY);
    }
    
    private ProductDTO productToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setImageLink(product.getImageLink());
        return dto;
    }
    
    private Product dtoToProduct(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscount(dto.getDiscount());
        product.setImageLink(dto.getImageLink());
        return product;
    }
}

