package com.example.ecommerce.service;

import com.example.ecommerce.DTO.ProductDTO;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String PRODUCT_KEY_PREFIX = "products::";
    private final String STOCK_KEY_PREFIX = "stock:";

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
    // -------------------
    // GET from Cache + Fallback
    // -------------------

    public List<Product> getAllProductsFromCache(){
        Object cached = redisTemplate.opsForValue().get(PRODUCT_KEY_PREFIX + "all");
        if (cached == null) {
            return Collections.emptyList();
        }

        return (List<Product>) cached;
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
            Product product = new Product();
            product.setId(dto.getId());
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setStock(stock != null ? stock : 0);

            return product;
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
        cacheProduct(saved); // also cache it
        return saved;
    }

    public Product updateProduct(Product product) {
        if (!productRepository.existsById(product.getId())) {
            throw new RuntimeException("Product not found for update");
        }
        Product updated = productRepository.save(product);
        cacheProduct(updated); // update cache
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
        return ResponseEntity.ok("Product deleted successfully");

    }

    // -------------------
    // Helper methods for cache ops
    // -------------------
    private void cacheProduct(Product product) {
        String productKey = PRODUCT_KEY_PREFIX + product.getId();

        // Convert to DTO
        ProductDTO dto = new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice()
        );

        redisTemplate.opsForValue().set(productKey, dto);
        redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + product.getId(), product.getStock());
    }

    private void removeProductFromCache(Long productId) {
        redisTemplate.delete(PRODUCT_KEY_PREFIX + productId);
        redisTemplate.delete(STOCK_KEY_PREFIX + productId);
    }

}
