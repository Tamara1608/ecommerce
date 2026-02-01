package com.example.ecommerce.Shared.preloader;

import com.example.ecommerce.Product.IProductService;
import com.example.ecommerce.Product.ProductDTO;
import com.example.ecommerce.Product.entity.Product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


@Component
public class ProductPreload implements CommandLineRunner {

    private final IProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PRODUCT_KEY_PREFIX = "products::";
    private static final String STOCK_KEY_PREFIX = "stock:";
    private static final String PRODUCT_IDS_KEY = "products:all_ids";
    private static final String ALL_PRODUCTS_KEY = "products:all";

    public ProductPreload(
            @Qualifier("dbProductService") IProductService productService,
            RedisTemplate<String, Object> redisTemplate) {
        this.productService = productService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Get all products from database
        List<Product> products = productService.findAll();
        List<ProductDTO> productDTOList = new ArrayList<>();

        for (Product product : products) {
            // Extract currentStock from Stock entity
            Integer currentStock = 0;
            if (product.getStock() != null && product.getStock().getCurrentValue() != null) {
                currentStock = product.getStock().getCurrentValue();
            }

            // Create ProductDTO for metadata caching
            ProductDTO dto = new ProductDTO(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getDiscount(),
                    product.getImageLink()
            );

            // Cache product metadata (ProductDTO without stock)
            redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), dto);

            // Cache stock counter separately
            redisTemplate.opsForValue().set(STOCK_KEY_PREFIX + product.getId(), currentStock);

            // Cache for retrieving all ids of product entity
            redisTemplate.opsForSet().add(PRODUCT_IDS_KEY, product.getId().toString());
            
            // Add DTO to list for caching all products
            productDTOList.add(dto);
        }
        
        // Cache the entire DTO list to avoid N+1 problem
        if (!productDTOList.isEmpty()) {
            redisTemplate.opsForValue().set(ALL_PRODUCTS_KEY, productDTOList);
        }
        
        System.out.println("Redis preloaded with " + productDTOList.size() + " products (including cached DTO list)");
    }
}
