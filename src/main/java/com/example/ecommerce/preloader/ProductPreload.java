package com.example.ecommerce.preloader;

import com.example.ecommerce.DTO.ProductDTO;
import com.example.ecommerce.DTO.ProductResponseDTO;
import com.example.ecommerce.service.ProductService;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class ProductPreload implements CommandLineRunner {

    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    private final String PRODUCT_KEY_PREFIX = "products::";
    private final String STOCK_KEY_PREFIX = "stock:";
    private final String PRODUCT_IDS_KEY = "products:all_ids";
    private final String ALL_PRODUCTS_KEY = "products:all_list";

    public ProductPreload(ProductService productService, RedisTemplate<String, Object> redisTemplate) {
        this.productService = productService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // getAllProducts() now returns List<ProductResponseDTO> (with stock info)
        List<ProductResponseDTO> products = productService.getAllProductsFromDB();
        List<ProductDTO> productDTOList = new java.util.ArrayList<>();

        for (ProductResponseDTO responseDTO : products) {
            // Extract currentStock from ProductResponseDTO
            Integer currentStock = (responseDTO.getCurrentStock() != null) 
                    ? responseDTO.getCurrentStock() 
                    : 0;

            // Create ProductDTO (without stock fields) for metadata caching
            ProductDTO dto = new ProductDTO(
                    responseDTO.getId(),
                    responseDTO.getName(),
                    responseDTO.getDescription(),
                    responseDTO.getPrice(),
                    responseDTO.getDiscount(),
                    responseDTO.getImageLink()
            );

            // Cache product metadata (ProductDTO without stock)
            redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + responseDTO.getId(), dto);

            // Cache stock counter separately
            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + responseDTO.getId(), currentStock);

            // Cache for retrieving all ids of product entity
            redisTemplate.opsForSet().add(PRODUCT_IDS_KEY, responseDTO.getId().toString());
            
            // Add DTO to list for caching all products
            productDTOList.add(dto);
        }
        
        // Cache the entire DTO list to avoid N+1 problem
        // Using DTOs avoids serialization issues with JPA lazy-loaded relationships
        if (!productDTOList.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_PRODUCTS_KEY, productDTOList);
        }
        
        System.out.println("Redis preloaded with " + productDTOList.size() + " products (including cached DTO list)");
    }
}
