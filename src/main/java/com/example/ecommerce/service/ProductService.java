package com.example.ecommerce.service;

import com.example.ecommerce.DTO.ProductDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.Stock;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String PRODUCT_KEY_PREFIX = "products::";
    private final String STOCK_KEY_PREFIX = "stock:";
    private final String PRODUCT_IDS_KEY = "products:all_ids";
    private final String ALL_PRODUCTS_KEY = "products:all_list"; // Cached list of all products

    // -------------------
    // GET from DB directly
    // -------------------

    public Product getProductFromDB (Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,  "Product not found!"
                ));
    }



    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Production version of API get products
    public List<Product> getAllProductsProdVersion() {
        // First try to get from cache
        Object cached = redisTemplate.opsForValue().get(ALL_PRODUCTS_KEY);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<ProductDTO> cachedDTOs = (List<ProductDTO>) cached;
            if (cachedDTOs != null && !cachedDTOs.isEmpty()) {
                return convertDTOsToProducts(cachedDTOs);
            }
        }
        
        // If not in cache, fetch from DB and cache it
        List<Product> products = productRepository.findAll();
        if (products != null && !products.isEmpty()) {
            cacheAllProducts(products);
        }
        return products;
    }

    // -------------------
    // GET from Cache + Fallback
    // -------------------

    public List<Product> getAllProductsFromCache() {
        // First try to get the cached "all products" DTO list (single Redis call)
        Object cached = redisTemplate.opsForValue().get(ALL_PRODUCTS_KEY);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<ProductDTO> cachedDTOs = (List<ProductDTO>) cached;
            if (cachedDTOs != null && !cachedDTOs.isEmpty()) {
                // Convert DTOs to Products with stock
                return convertDTOsToProducts(cachedDTOs);
            }
        }

        // Fallback: try to reconstruct from individual product caches
        Set<Object> ids = redisTemplate.opsForSet().members(PRODUCT_IDS_KEY);

        if (ids == null || ids.isEmpty()) {
            // If cache is empty, try to load from DB and cache
            List<Product> dbProducts = productRepository.findAll();
            if (dbProducts != null && !dbProducts.isEmpty()) {
                cacheAllProducts(dbProducts);
                return dbProducts;
            }
            return Collections.emptyList();
        }

        List<ProductDTO> dtos = new ArrayList<>();

        for (Object idObj : ids) {
            String idStr = idObj.toString();
            String productKey = PRODUCT_KEY_PREFIX + idStr;

            Object cachedProduct = redisTemplate.opsForValue().get(productKey);
            if (cachedProduct instanceof ProductDTO dto) {
                dtos.add(dto);
            }
        }

        // Cache the reconstructed DTO list for future use (avoids N+1 on next call)
        if (!dtos.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_PRODUCTS_KEY, dtos);
            return convertDTOsToProducts(dtos);
        }

        return Collections.emptyList();
    }

    // Get product from Redis or fallback to DB
    public Product getProductFromCache(Long productId) {
        String productKey = PRODUCT_KEY_PREFIX + productId;
        Object cached = redisTemplate.opsForValue().get(productKey);

        if (cached != null) {
            ProductDTO dto = (ProductDTO) cached;

            // Fetch stock separately
            Integer stock = (Integer) redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + productId);

            // Convert back to Product entity for API response
            return createProduct(dto, stock);
        }

        // Fallback to DB + write in cache
        Product dbProduct = getProductFromDB(productId);
        if (dbProduct != null) {cacheProduct(dbProduct);}
        return dbProduct;
    }


    // -------------------
    // CREATE / UPDATE
    // -------------------

    public Product createProduct(Product product) {
        Product saved = productRepository.save(product);
        cacheProduct(saved); // cache individual product metadata and stock
        invalidateAllProductsCache(); // invalidate the all products list cache
        return saved;
    }

    public Product updateProduct(Product product) {
        if (!productRepository.existsById(product.getId())) {
            throw new RuntimeException("Product not found for update");
        }
        Product updated = productRepository.save(product);
        cacheProduct(updated); // update individual product cache
        invalidateAllProductsCache(); // invalidate the all products list cache
        return updated;
    }

    // -------------------
    // DELETE
    // -------------------

    public ResponseEntity<String>  deleteProduct(Long id) {
        if (!productRepository.existsById(id)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Product not found");
        }
        productRepository.deleteById(id);
        removeProductFromCache(id);
        invalidateAllProductsCache(); // invalidate the all products list cache
        return ResponseEntity.ok("Product deleted successfully");

    }

    // --------------------
    // Helper methods
    // --------------------

    private void cacheProduct(Product product) {
        String productKey = PRODUCT_KEY_PREFIX + product.getId();

        // Convert to DTO
        ProductDTO dto = new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDiscount(),
                product.getImageLink()
        );

        redisTemplate.opsForValue().set(productKey, dto); // metadata
        redisTemplate.opsForSet().add(PRODUCT_IDS_KEY, product.getId().toString()); //all keys

        // Cache stock separately (if available)
        if (product.getStock() != null) {
            Integer currentStock = (product.getStock().getCurrentValue() != null)
                    ? product.getStock().getCurrentValue() : 0;
            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + product.getId(), currentStock);
        }
    }

    private void removeProductFromCache(Long productId) {
        redisTemplate.delete(PRODUCT_KEY_PREFIX + productId);
        redisTemplate.delete(STOCK_KEY_PREFIX + productId);
        redisTemplate.opsForSet().remove(PRODUCT_IDS_KEY, productId.toString()); // Fix: remove from set, not delete key
    }

   
    private void cacheAllProducts(List<Product> products) {
        // First, ensure all individual products are cached (metadata + stock)
        for (Product product : products) {
            cacheProduct(product);
        }
        
        // Create DTO list for caching
        List<ProductDTO> dtoList = new ArrayList<>();
        for (Product product : products) {
            ProductDTO dto = new ProductDTO(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getDiscount(),
                    product.getImageLink()
            );
            dtoList.add(dto);
        }
        
        // Cache the DTO list (single Redis call for future getAllProductsFromCache calls)
        redisTemplate.opsForValue().set(ALL_PRODUCTS_KEY, dtoList);
    }
    
  
    private List<Product> convertDTOsToProducts(List<ProductDTO> dtos) {
        if (dtos.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Build list of all stock keys in the SAME ORDER as dtos
        // This ensures stockValues[i] corresponds to dtos[i]
        List<String> stockKeys = new ArrayList<>();
        for (ProductDTO dto : dtos) {
            stockKeys.add(STOCK_KEY_PREFIX + dto.getId());
        }
        
        // Batch fetch all stock values in a single Redis call (avoids N+1 problem)
        // multiGet() returns values in the SAME ORDER as stockKeys
        List<Object> stockValues = redisTemplate.opsForValue().multiGet(stockKeys);
        
        // Convert DTOs to Products with their corresponding stock values
        // Index i matches because: dtos[i] → stockKeys[i] → stockValues[i]
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < dtos.size(); i++) {
            ProductDTO dto = dtos.get(i);
            // Get stock value from batch result (null if not found or key doesn't exist)
            Integer stock = (stockValues != null && i < stockValues.size() && stockValues.get(i) != null)
                    ? (Integer) stockValues.get(i)
                    : null;
            Product p = createProduct(dto, stock);
            products.add(p);
        }
        
        return products;
    }

   
    private void invalidateAllProductsCache() {
        redisTemplate.delete(ALL_PRODUCTS_KEY);
    }

    private Product createProduct(ProductDTO dto, Integer stock) {
        Product product = new Product();

        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setDiscount(dto.getDiscount());
        product.setImageLink(dto.getImageLink());

        Stock s = new Stock();
        int stockValue = (stock != null) ? stock : 0;
        s.setCurrentValue(stockValue);
        s.setTotalStock(stockValue);

        product.setStock(s);

        return product;
    }

}

